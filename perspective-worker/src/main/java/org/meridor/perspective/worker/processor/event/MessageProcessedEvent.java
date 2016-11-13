package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.backend.messaging.Message;

public class MessageProcessedEvent extends MessageEvent {
    public MessageProcessedEvent(Message message) {
        super(message);
    }
}
