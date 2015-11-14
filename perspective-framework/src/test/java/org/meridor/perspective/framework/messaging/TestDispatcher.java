package org.meridor.perspective.framework.messaging;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestDispatcher implements Dispatcher {
    
    private List<Message> messages = new ArrayList<>();
    
    @Override
    public void dispatch(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return messages;
    }
}
