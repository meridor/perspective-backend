package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.backend.messaging.Destination;
import org.meridor.perspective.backend.messaging.Message;
import org.meridor.perspective.backend.messaging.Producer;
import org.meridor.perspective.beans.Letter;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.common.events.EventListener;
import org.meridor.perspective.events.BaseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;

import static org.meridor.perspective.backend.messaging.MessageUtils.canRetry;
import static org.meridor.perspective.backend.messaging.MessageUtils.message;
import static org.meridor.perspective.beans.DestinationName.MAIL;
import static org.meridor.perspective.beans.LetterType.OK;
import static org.meridor.perspective.events.EventFactory.now;
import static org.meridor.perspective.events.EventFactory.uuid;

@Component
public abstract class OperationFailureListener<T extends BaseEvent> implements EventListener<MessageNotProcessedEvent> {
    
    @Autowired
    private EventBus eventBus;
    
    @Destination(MAIL)
    private Producer producer;
    
    @PostConstruct
    public void init(){
        eventBus.addListener(MessageNotProcessedEvent.class, this);
    }

    @Override
    public void onEvent(MessageNotProcessedEvent event) {
        Message message = event.getMessage();
        if (message != null && !canRetry(message)) {
            Serializable payload = message.getPayload();
            Class<T> eventClass = getEventClass();
            if (payload != null && eventClass.isAssignableFrom(payload.getClass())) {
                processEvent(eventClass.cast(payload));
            }
        }
    }
    
    protected abstract void processEvent(T event);
    
    protected abstract Class<T> getEventClass();

    void sendLetter(String text) {
        Letter letter = new Letter();
        letter.setId(uuid());
        letter.setText(text);
        letter.setType(OK);
        letter.setTimestamp(now());
        producer.produce(message(letter));
    }
}
