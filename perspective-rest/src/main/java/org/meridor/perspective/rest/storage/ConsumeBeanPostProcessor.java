package org.meridor.perspective.rest.storage;

import org.meridor.perspective.beans.DestinationName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class ConsumeBeanPostProcessor implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumeBeanPostProcessor.class);

    @Value("${perspective.queue.shutdown.timeout}")
    private int shutdownTimeout;

    @Autowired
    private Storage storage;
    
    private ExecutorService executorService;

    private volatile boolean canExecute = true;
    
    private Map<Runnable, Integer> runnables = new HashMap<>();
    
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
                    Consume annotation = m.getAnnotation(Consume.class);
                    DestinationName destinationName = annotation.queueName();
                    int numConsumers = annotation.numConsumers();

                    Optional<Runnable> runnable = getConsumerRunnable(bean, m, destinationName);
                    if (runnable.isPresent()) {
                        runnables.put(runnable.get(), numConsumers);
                    }
                },
                m -> m.isAnnotationPresent(Consume.class)
        );
        return bean;
    }

    public Optional<Runnable> getConsumerRunnable(Object bean, Method method, DestinationName destinationName) {
        try {

            if (method.getParameterCount() != 1) {
                LOG.debug("Will not consume to method {} because it has more than 1 parameter.");
                return Optional.empty();
            }

            String storageKey = (destinationName != DestinationName.UNDEFINED) ?
                    destinationName.value() :
                    bean.getClass().getCanonicalName();
            
            Runnable runnable = () -> {
                while (canExecute) {
                    try {
                        if (!storage.isAvailable()) {
                            LOG.debug("Stopping consumer thread {} as storage is not available", Thread.currentThread());
                            return;
                        }
                        BlockingQueue<Object> queue = storage.getQueue(storageKey);
                        Object item = queue.poll(1000, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            Class<?> parameterType = method.getParameterTypes()[0];
                            if (parameterType.isAssignableFrom(item.getClass())) {
                                method.invoke(bean, item);
                            }
                        }
                    } catch (Exception e) {
                        LOG.debug("Failed to consume message from queue " + destinationName, e);
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
        canExecute = false;
        if (executorService != null) {
            LOG.info("Shutting down consumers");
            executorService.shutdown();
            executorService.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        int threadsCount = runnables.values().stream().collect(Collectors.summingInt(cn -> cn));
        if (threadsCount > 0) {
            LOG.debug("Will use {} consumer threads in total", threadsCount);
            executorService = Executors.newFixedThreadPool(threadsCount);
            for (Runnable runnable : runnables.keySet()) {
                Integer numConsumers = runnables.get(runnable);
                for (int threadNumber = 0; threadNumber <= numConsumers - 1; threadNumber++) {
                    executorService.submit(runnable);
                }
            }
        }
    }
}
