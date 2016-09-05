package org.meridor.perspective.framework.messaging.impl;

import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.framework.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

import static org.meridor.perspective.framework.messaging.MessageUtils.getRealQueueName;

public class ProducerImpl implements Producer {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerImpl.class);

    private final String queueName;

    private final Storage storage;

    public ProducerImpl(String queueName, Storage storage) {
        if (queueName == null) {
            throw new IllegalArgumentException("Queue name can't be null");
        }
        this.queueName = queueName;
        this.storage = storage;
    }

    @Override
    public void produce(Message message) {
        try {
            String realQueueName = getRealQueueName(queueName, message.getCloudType());
            LOG.trace("Putting message {} to queue \"{}\"", message, realQueueName);
            if (!storage.isAvailable()) {
                LOG.trace("Storage is not available. Will not put {} to queue {}.", message, realQueueName);
                return;
            }
            BlockingQueue<Object> queue = storage.getQueue(realQueueName);
            queue.put(message);
        } catch (Exception e) {
            LOG.error(String.format("Failed to put message to queue \"%s\"", queueName), e);
        }
    }

    public String getQueueName() {
        return queueName;
    }
}
