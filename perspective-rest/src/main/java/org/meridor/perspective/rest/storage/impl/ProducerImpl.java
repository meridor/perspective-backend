package org.meridor.perspective.rest.storage.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import org.meridor.perspective.rest.storage.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProducerImpl implements Producer {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerImpl.class);

    private final String storageKey;
    
    private final HazelcastInstance hazelcastInstance;

    public ProducerImpl(String storageKey, HazelcastInstance hazelcastInstance) {
        this.storageKey = storageKey;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void produce(Object data) {
        if (storageKey == null) {
            throw new IllegalStateException("Storage key can't be null");
        }
        IQueue<Object> queue = hazelcastInstance.getQueue(storageKey);
        LOG.debug("Putting value {} to queue {}", data, storageKey);
        try {
            if (data instanceof List) {
                for (Object value : (List) data) {
                    queue.put(value);
                }
            } else {
                queue.put(data);
            }
        } catch (InterruptedException e) {
            LOG.error("Failed to put data to queue " + storageKey, e);
        }
    }
    
}
