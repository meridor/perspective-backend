package org.meridor.perspective.rest.storage.impl;

import org.meridor.perspective.rest.storage.Producer;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ProducerImpl implements Producer {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerImpl.class);

    private final String queueName;
    
    private final Storage storage;

    public ProducerImpl(String queueName, Storage storage) {
        this.queueName = queueName;
        this.storage = storage;
    }

    @Override
    public void produce(Object data) {
        if (queueName == null) {
            throw new IllegalStateException("Storage key can't be null");
        }
        try {
            LOG.debug("Putting value {} to queue {}", data, queueName);
            if (!storage.isAvailable()) {
                LOG.debug("Storage is not available. Will not put {} to queue {}.", data, queueName);
                return;
            }
            BlockingQueue<Object> queue = storage.getQueue(queueName);
            if (data instanceof List) {
                for (Object value : (List) data) {
                    queue.put(value);
                }
            } else {
                queue.put(data);
            }
        } catch (Exception e) {
            LOG.error("Failed to put data to queue " + queueName, e);
        }
    }
    
}
