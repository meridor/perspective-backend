package org.meridor.perspective.shell.wizard;

import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.Optional;

import static org.meridor.perspective.shell.misc.LoggingUtils.error;
import static org.meridor.perspective.shell.misc.LoggingUtils.ok;

public interface Step {
    
    boolean run();
    
    String getAnswer();
    
    String getMessage();
    
    default Optional<String> getDefaultAnswer() {
        return Optional.empty();
    }

    default String waitForAnswer() {
        try {
            ConsoleReader consoleReader = new ConsoleReader();
            String answer = consoleReader.readLine();
            Optional<String> defaultAnswer = getDefaultAnswer();
            return answer.isEmpty() && defaultAnswer.isPresent() ?
                    defaultAnswer.get() :
                    answer;
        } catch (IOException e) {
            error(String.format("Error while getting input: %s" ,e.getMessage()));
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
