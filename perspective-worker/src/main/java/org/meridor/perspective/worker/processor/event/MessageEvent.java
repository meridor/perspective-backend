package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.backend.messaging.Message;

public abstract class MessageEvent {
    
    private final Message message;

    public MessageEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
