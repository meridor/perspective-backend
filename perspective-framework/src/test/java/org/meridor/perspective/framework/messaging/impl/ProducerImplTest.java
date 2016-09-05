package org.meridor.perspective.framework.messaging.impl;

import org.junit.Test;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.framework.messaging.TestStorage;
import org.meridor.perspective.framework.storage.Storage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.framework.messaging.MessageUtils.getRealQueueName;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

public class ProducerImplTest {

    private static final String QUEUE_NAME = "test"; 
    private static final String PAYLOAD = "payload";
    
    @Test
    public void testProduce() throws Exception {
        Storage storage = new TestStorage();
        Producer producer = new ProducerImpl(QUEUE_NAME, storage);
        Message msg = message(CloudType.MOCK, PAYLOAD); 
        producer.produce(msg);
        BlockingQueue<Object> queue = storage.getQueue(getRealQueueName(QUEUE_NAME, CloudType.MOCK));
        Object data = queue.poll(1, TimeUnit.SECONDS);
        assertThat(data, is(notNullValue()));
        assertThat(data, is(instanceOf(Message.class)));
        Message receivedMsg = (Message) data;
        assertThat(msg.getId(), equalTo(receivedMsg.getId()));
        assertThat(msg.getCloudType(), equalTo(receivedMsg.getCloudType()));
        assertThat(msg.getPayload(), equalTo(receivedMsg.getPayload()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNoQueueName() {
        new ProducerImpl(null, new TestStorage());
    }
}