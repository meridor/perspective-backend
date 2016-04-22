package org.meridor.perspective.framework.messaging;

import org.junit.Test;
import org.meridor.perspective.config.CloudType;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.framework.messaging.MessageUtils.getRealQueueName;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

public class MessageUtilsTest {

    @Test
    public void testMessage() throws Exception {
        String payload = "payload";
        Message message = message(CloudType.MOCK, payload, 5);
        assertThat(message.getId(), is(notNullValue()));
        assertThat(message.getCloudType(), is(notNullValue()));
        assertThat(message.getPayload(), equalTo(payload));
        assertThat(message.getTtl(), equalTo(5));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCloudTypeMessage() {
        message(null, null);
    }

    @Test
    public void testGetRealQueueName() throws Exception {
        assertThat(getRealQueueName("test", CloudType.MOCK), equalTo("mock_test"));
    }
}