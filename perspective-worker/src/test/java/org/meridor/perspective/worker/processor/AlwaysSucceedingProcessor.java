package org.meridor.perspective.worker.processor;

import org.meridor.perspective.framework.messaging.Message;

public class AlwaysSucceedingProcessor implements Processor {
    
    private boolean processed;
    
    @Override
    public void process(Message message) {
        processed = true;
    }

    @Override
    public boolean isPayloadSupported(Class<?> payloadClass) {
        return String.class.isAssignableFrom(payloadClass);
    }

    public boolean wasProcessed() {
        return processed;
    }
}
