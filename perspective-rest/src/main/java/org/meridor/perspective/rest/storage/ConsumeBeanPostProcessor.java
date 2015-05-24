package org.meridor.perspective.rest.storage;

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
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.meridor.perspective.rest.storage.AspectUtils.getAnnotationParameter;

@Component
public class ConsumeBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumeBeanPostProcessor.class);

    @Value("${perspective.queue.thread.count}")
    private int threadsCount;
    
    @Value("${perspective.queue.shutdown.timeout}")
    private int shutdownTimeout;

    @Autowired
    private Storage storage;

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
                    Optional<Runnable> runnable = getConsumerRunnable(bean, m, switcher);
                    if (runnable.isPresent()) {
                        for (int threadNumber = 0; threadNumber <= threadsCount - 1; threadNumber++) {
                            executorService.submit(runnable.get());
                        }
                        executorServices.put(m, executorService);
                    }
                },
                m -> m.isAnnotationPresent(Consume.class)
        );
        return bean;
    }

    public Optional<Runnable> getConsumerRunnable(Object bean, Method method, AtomicBoolean switcher) {
        try {
            Annotation annotation = method.getAnnotation(Consume.class);
            final String keyName = getAnnotationParameter(
                    annotation,
                    Consume.STORAGE_KEY,
                    t -> !t.isEmpty(),
                    bean.getClass().getCanonicalName()
            );
            Runnable runnable = () -> {
                while (switcher.get()) {
                    try {
                        BlockingQueue<Object> queue = storage.getQueue(keyName);
                        Object item = queue.take(); //TODO: this one causes long application stop
                        method.invoke(bean, item);
                    } catch (Exception e) {
                        LOG.debug("Failed to consume message from queue " + keyName, e);
                    }
                }
            };
            return Optional.of(runnable);
        } catch (Exception e) {
            LOG.debug("Failed to get queue name for method {} of bean {}", method, bean);
            return Optional.empty();
        }
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        LOG.info("Shutting down {} consumers", executorServices.size());
        for (AtomicBoolean switcher : switchers.values()) {
            switcher.set(false);
        }
        ExecutorService shutdownExecutorService = Executors.newFixedThreadPool(executorServices.size());
        for (Method method : executorServices.keySet()) {
            LOG.debug("Shutting down consumer for method {} (waiting for {} milliseconds)", method, shutdownTimeout);
            ExecutorService executorService = executorServices.get(method);
            //TODO: investigate why it does not stop (probably we need to stop scheduler before this one)
            shutdownExecutorService.submit(
                    () -> executorService.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS)
            );
        }
        shutdownExecutorService.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
    }
    
}
