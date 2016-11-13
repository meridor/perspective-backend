package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.messaging.Message;

public class AlwaysFailingProcessor implements Processor {
    @Override
    public void process(Message message) {
        throw new RuntimeException();
    }

    @Override
    public boolean isPayloadSupported(Class<?> payloadClass) {
        return Integer.class.isAssignableFrom(payloadClass);
    }
}
