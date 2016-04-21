package org.meridor.perspective.shell.common.events;

import java.util.Optional;

public class PromptChangedEvent {
    
    private final String newPrompt;

    public PromptChangedEvent() {
        this.newPrompt = null;
    }

    public PromptChangedEvent(String newPrompt) {
        this.newPrompt = newPrompt;
    }

    public Optional<String> getNewPrompt() {
        return Optional.ofNullable(newPrompt);
    }
}
