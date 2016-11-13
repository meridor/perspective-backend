package org.meridor.perspective.backend.messaging;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TestDispatcher implements Dispatcher {
    
    private List<Message> messages = new ArrayList<>();
    
    @Override
    public Optional<Message> dispatch(Message message) {
        messages.add(message);
        return Optional.of(message); //Always returns message back to consumer to cause retries
    }

    public List<Message> getMessages() {
        return messages;
    }
}
