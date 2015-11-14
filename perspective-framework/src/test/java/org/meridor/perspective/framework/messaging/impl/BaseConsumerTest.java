package org.meridor.perspective.framework.messaging.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.messaging.TestConsumer;
import org.meridor.perspective.framework.messaging.TestDispatcher;
import org.meridor.perspective.framework.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@ContextConfiguration(locations = "/META-INF/spring/mocked-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseConsumerTest {
    
    private static final String PAYLOAD = "payload";
    
    @Autowired
    private Storage storage;
    
    @Autowired
    private TestDispatcher testDispatcher;

    private void putToQueue(Object message) throws InterruptedException {
        BlockingQueue<Object> queue = storage.getQueue(TestConsumer.STORAGE_KEY);
        queue.put(message);
    }
    
    @Test
    public void testConsume() throws Exception {
        Message msg = message(CloudType.MOCK, PAYLOAD);
        putToQueue(msg);
        Thread.sleep(1000);
        List<Message> messagesList = testDispatcher.getMessages();
        assertThat(messagesList, hasSize(1));
        Message message = messagesList.get(0);
        assertThat(message.getId(), equalTo(msg.getId()));
        assertThat(message.getCloudType(), equalTo(msg.getCloudType()));
        assertThat(message.getPayload(), equalTo(msg.getPayload()));
    }

}