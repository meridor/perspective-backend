package org.meridor.perspective.backend.messaging;

import java.util.Optional;

public interface Dispatcher {

    /**
     * Sends message to respective processor
     * @param message message to dispatch 
     * @return empty if ok or Optional.of(message) if error occurred
     */
    Optional<Message> dispatch(Message message);

}
