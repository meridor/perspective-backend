package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.backend.messaging.Destination;
import org.meridor.perspective.backend.messaging.Producer;
import org.meridor.perspective.beans.Letter;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.backend.messaging.MessageUtils.message;
import static org.meridor.perspective.beans.DestinationName.MAIL;
import static org.meridor.perspective.beans.LetterType.OK;
import static org.meridor.perspective.events.EventFactory.now;
import static org.meridor.perspective.events.EventFactory.uuid;

@Component
public class MailSender {

    @Destination(MAIL)
    private Producer producer;

    public void sendLetter(String text) {
        Letter letter = new Letter();
        letter.setId(uuid());
        letter.setText(text);
        letter.setType(OK);
        letter.setTimestamp(now());
        producer.produce(message(letter));
    }

}
