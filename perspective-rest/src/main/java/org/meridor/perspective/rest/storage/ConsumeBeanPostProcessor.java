package org.meridor.perspective.rest.storage;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import org.meridor.perspective.rest.aspects.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.meridor.perspective.rest.aspects.AspectUtils.getAnnotationParameter;

@Component
public class ConsumeBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumeBeanPostProcessor.class);

    @Value("${perspective.queue.thread.count}")
    private int threadsCount;
    
    @Value("${perspective.queue.shutdown.timeout}")
    private int shutdownTimeout;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    private Map<Method, ExecutorService> executorServices = new HashMap<>();
    
    private Map<Method, AtomicBoolean> switchers = new HashMap<>();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class cls = bean.getClass();
        ReflectionUtils.doWithMethods(
                cls,
                m -> {
                    //TODO: decide whether we need to change this one to another thread pool!!!
                    ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
                    AtomicBoolean switcher = new AtomicBoolean(true);
                    switchers.put(m, switcher);
                    Runnable runnable = getConsumerRunnable(bean, m, switcher);
                    executorService.submit(runnable);
                    executorServices.put(m, executorService);
                },
                m -> m.isAnnotationPresent(Consume.class)
        );
        return bean;
    }

    public Runnable getConsumerRunnable(Object bean, Method method, AtomicBoolean switcher) {
        String keyName = null;
        try {
            Annotation annotation = method.getAnnotation(Consume.class);
            keyName = getAnnotationParameter(
                    annotation,
                    Consume.STORAGE_KEY,
                    t -> !t.isEmpty(),
                    bean.getClass().getCanonicalName()
            );
        } catch (Exception e) {
            LOG.debug("Failed to get queue name for method {} of bean {}", method, bean);
        }
        IQueue<Object> queue = hazelcastInstance.getQueue(keyName);
        return () -> {
            while (switcher.get()) {
                try {
                    Object item = queue.take();
                    method.invoke(bean, item);
                } catch (Exception e) {
                    LOG.debug("Failed to consume message from queue " + queue.getName(), e);
                }
            }
        };
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        LOG.info("Shutting down consuming threads for {} methods", executorServices.size());
        for (AtomicBoolean switcher : switchers.values()) {
            switcher.set(false);
        }
        for (Method method : executorServices.keySet()) {
            LOG.debug("Shutting down executor service for method {}", method);
            ExecutorService executorService = executorServices.get(method);
            //TODO: investigate why it does not stop
            executorService.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
        }
    }
    
}
