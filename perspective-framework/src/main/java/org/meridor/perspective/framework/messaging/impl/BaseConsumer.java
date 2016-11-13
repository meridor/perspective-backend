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
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.meridor.perspective.framework.messaging.MessageUtils.retry;

@Component
public abstract class BaseConsumer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseConsumer.class);

    @Value("${perspective.messaging.shutdown.timeout:30000}")
    private int shutdownTimeout;

    @Value("${perspective.messaging.polling.delay:1000}")
    private int pollingDelay;
    
    @Value("${perspective.messaging.tolerable.queue.size:1000}")
    private int tolerableQueueSize;

    @Autowired
    private Storage storage;

    private ExecutorService executorService;

    private volatile boolean canExecute = true;

    protected abstract String getStorageKey();
    
    protected abstract int getParallelConsumers();
    
    protected abstract Dispatcher getDispatcher();

    private Runnable getRunnable() {
        return () -> {
            while (canExecute) {
                String storageKey = getStorageKey();
                try {
                    if (!storage.isAvailable()) {
                        LOG.debug("Stopping consuming from queue \"{}\" as storage is not available", storageKey);
                        return;
                    }
                    BlockingQueue<Object> queue = storage.getQueue(storageKey);
                    int queueSize = queue.size();
                    Object item = queue.poll(pollingDelay, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        if (item instanceof Message) {
                            if (queueSize > tolerableQueueSize) {
                                LOG.warn("Messages queue \"{}\" size = {} exceeds tolerable size = {}. This can be a signal to increase total number of workers.", storageKey, queueSize, tolerableQueueSize);
                            }
                            Message message = (Message) item;
                            LOG.trace("Consumed message {} from queue = {}", message, storageKey);
                            Optional<Message> notProcessedMessage = getDispatcher().dispatch(message);
                            if (notProcessedMessage.isPresent()) {
                                Optional<Message> nextRetry = retry(notProcessedMessage.get());
                                if (nextRetry.isPresent()) {
                                    LOG.trace("Retrying not processed message = {}", message);
                                    queue.put(nextRetry.get());
                                }
                            }
                        } else {
                            LOG.warn("Skipping {} as it is not a message", item);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(String.format("Failed to consume message from queue %s", storageKey), e);
                }
            }
        };
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        canExecute = false;
        if (executorService != null) {
            LOG.debug("Shutting down consuming from queue \"{}\"", getStorageKey());
            executorService.shutdown();
            executorService.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final int parallelConsumers = getParallelConsumers();
        LOG.debug("Will use {} parallel consumers for queue \"{}\"", parallelConsumers, getStorageKey());
        executorService = Executors.newFixedThreadPool(parallelConsumers);
        for (int threadNumber = 0; threadNumber <= parallelConsumers - 1; threadNumber++) {
            Runnable runnable = getRunnable();
            executorService.submit(runnable);
        }
    }

}
