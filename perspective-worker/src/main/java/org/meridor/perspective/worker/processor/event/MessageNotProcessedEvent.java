package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.backend.messaging.Message;

public class MessageNotProcessedEvent extends MessageEvent {
    public MessageNotProcessedEvent(Message message) {
        super(message);
    }
}
