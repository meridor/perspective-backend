package org.meridor.perspective.worker.processor;

import org.meridor.perspective.framework.messaging.Message;

public interface Processor {

    void process(Message message);
    
    boolean isPayloadSupported(Class<?> payloadClass);

}
