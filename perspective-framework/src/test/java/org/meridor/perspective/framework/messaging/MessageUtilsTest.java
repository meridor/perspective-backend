package org.meridor.perspective.framework.messaging;

import org.junit.Test;
import org.meridor.perspective.config.CloudType;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.framework.messaging.MessageUtils.getRealQueueName;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

public class MessageUtilsTest {

    @Test
    public void testMessage() throws Exception {
        String payload = "payload";
        Message message = message(CloudType.MOCK, payload);
        assertNotNull(message.getId());
        assertNotNull(message.getCloudType());
        assertThat(message.getPayload(), equalTo(payload));
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