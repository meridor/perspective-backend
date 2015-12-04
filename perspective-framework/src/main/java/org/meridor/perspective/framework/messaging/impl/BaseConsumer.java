package org.meridor.perspective.framework.messaging.impl;

import org.meridor.perspective.framework.messaging.Dispatcher;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public abstract class BaseConsumer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseConsumer.class);

    @Value("${perspective.messaging.shutdown.timeout}")
    private int shutdownTimeout;

    @Value("${perspective.messaging.parallel.consumers}")
    private int parallelConsumers;
    
    @Value("${perspective.messaging.polling.delay:1000}")
    private int pollingDelay;
    
    @Value("${perspective.messaging.tolerable.queue.size:1000}")
    private int tolerableQueueSize;

    @Autowired
    private Storage storage;

    @Autowired
    private Dispatcher dispatcher;

    private ExecutorService executorService;

    private volatile boolean canExecute = true;

    protected abstract String getStorageKey();

    private Runnable getRunnable() {
        return () -> {
            while (canExecute) {
                try {
                    if (!storage.isAvailable()) {
                        LOG.debug("Stopping consumer thread {} as storage is not available", Thread.currentThread());
                        return;
                    }
                    String storageKey = getStorageKey();
                    BlockingQueue<Object> queue = storage.getQueue(storageKey);
                    int queueSize = queue.size();
                    Object item = queue.poll(pollingDelay, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        if (item instanceof Message) {
                            if (queueSize > tolerableQueueSize) {
                                LOG.warn("Messages queue size = {} exceeds tolerable size = {}. This can be a signal to increase total number of workers.", queueSize, tolerableQueueSize);
                            }
                            Message message = (Message) item;
                            LOG.trace("Consumed message {}", message.getId());
                            dispatcher.dispatch(message);
                        } else {
                            LOG.warn("Skipping {} as it is not a message", item);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Failed to consume message from queue", e);
                }
            }
        };
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        canExecute = false;
        if (executorService != null) {
            LOG.debug("Shutting down consuming threads");
            executorService.shutdown();
            executorService.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.debug("Will use {} consuming threads", parallelConsumers);
        executorService = Executors.newFixedThreadPool(parallelConsumers);
        for (int threadNumber = 0; threadNumber <= parallelConsumers - 1; threadNumber++) {
            Runnable runnable = getRunnable();
            executorService.submit(runnable);
        }
    }

}
