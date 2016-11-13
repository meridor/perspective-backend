package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.messaging.Message;

public interface Processor {

    void process(Message message);
    
    boolean isPayloadSupported(Class<?> payloadClass);

}
