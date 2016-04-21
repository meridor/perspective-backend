package org.meridor.perspective.shell.interactive.wizard;

import java.util.Optional;

public interface Step {
    
    boolean run();
    
    String getAnswer();
    
    String getMessage();
    
    default boolean answerRequired() {
        return true;
    }
    
    default Optional<String> getDefaultAnswer() {
        return Optional.empty();
    }
    
}
