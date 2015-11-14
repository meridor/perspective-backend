package org.meridor.perspective.framework.messaging.impl;

import org.junit.Test;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.messaging.Message;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class MessageImplTest {
    
    @Test
    public void testGetPayloadAs() throws Exception {
        Optional<Number> numberPayload = getMessage().getPayload(Number.class);
        assertTrue(numberPayload.isPresent());
        assertThat(numberPayload.get(), equalTo(1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetPayloadInvalid() throws Exception {
        getMessage().getPayload(String.class);
    }
    
    @Test
    public void testGetPayloadEmpty() throws Exception {
        assertFalse(new MessageImpl(CloudType.MOCK, null).getPayload(String.class).isPresent());
    }
    
    private static Message getMessage() {
        return new MessageImpl(CloudType.MOCK, 1);
    }
}