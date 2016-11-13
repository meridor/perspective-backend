package org.meridor.perspective.backend.messaging;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.backend.messaging.MessageUtils.*;

public class MessageUtilsTest {

    @Test
    public void testRetry() {
        String payload = "anything";
        assertThat(retry(message(MOCK, payload, 1)).isPresent(), is(false));
        Message initialMessage = message(MOCK, payload, 42);
        Optional<Message> messageCandidate = retry(initialMessage);
        assertThat(messageCandidate.isPresent(), is(true));
        Message retryMessage = messageCandidate.get();
        assertThat(retryMessage.getId(), not(equalTo(initialMessage.getId())));
        assertThat(retryMessage.getCloudType(), equalTo(initialMessage.getCloudType()));
        assertThat(retryMessage.getPayload(), equalTo(initialMessage.getPayload()));
        assertThat(retryMessage.getTtl(), equalTo(initialMessage.getTtl() - 1));
    }
    
    @Test
    public void testMessageWithCloud() {
        String payload = "payload";
        Message message = message(MOCK, payload, 5);
        assertThat(message.getId(), is(notNullValue()));
        assertThat(message.getCloudType(), is(notNullValue()));
        assertThat(message.getPayload(), equalTo(payload));
        assertThat(message.getTtl(), equalTo(5));
    }
    
    @Test
    public void testMessageWithNoCloud() {
        String payload = "payload";
        Message message = message(payload);
        assertThat(message.getId(), is(notNullValue()));
        assertThat(message.getCloudType(), is(nullValue()));
        assertThat(message.getPayload(), equalTo(payload));
        assertThat(message.getTtl(), equalTo(1));
    }

    @Test
    public void testGetRealQueueName() {
        assertThat(getRealQueueName("test", MOCK), equalTo("mock_test"));
        assertThat(getRealQueueName("test", null), equalTo("test"));
    }
}