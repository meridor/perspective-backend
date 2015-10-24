package org.meridor.perspective.shell.wizard;

import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.Optional;

import static org.meridor.perspective.shell.misc.LoggingUtils.ok;

public interface Step {
    
    boolean run();
    
    String getAnswer();
    
    Optional<String> getDefaultAnswer();
    
    String getMessage();

    default String waitForInput() {
        try {
            ConsoleReader consoleReader = new ConsoleReader();
            return consoleReader.readLine();
        } catch (IOException e) {
            return "";
        }
    }
    
    default void printMessageWithDefaultAnswer() {
        Optional<String> defaultAnswer = getDefaultAnswer();
        String message = (defaultAnswer.isPresent()) ?
                String.format("%s [%s]", getMessage(), defaultAnswer.get()) :
                getMessage();
        ok(message);
    }

}
