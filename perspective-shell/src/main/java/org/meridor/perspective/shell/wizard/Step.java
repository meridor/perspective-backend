package org.meridor.perspective.shell.wizard;

import jline.console.ConsoleReader;
import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.repository.impl.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.meridor.perspective.shell.misc.LoggingUtils.error;
import static org.meridor.perspective.shell.misc.LoggingUtils.ok;

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

    default String waitForAnswer() {
        try {
            ConsoleReader consoleReader = new ConsoleReader();
            String answer = consoleReader.readLine();
            Optional<String> defaultAnswer = getDefaultAnswer();
            return answer.isEmpty() && defaultAnswer.isPresent() ?
                    defaultAnswer.get() :
                    answer;
        } catch (IOException e) {
            error(String.format("Error while getting input: %s", e.getMessage()));
            return "";
        }
    }
    
    default void printMessageWithDefaultAnswer() {
        Optional<String> defaultAnswer = getDefaultAnswer();
        String message = getMessage();
        if (defaultAnswer.isPresent()) {
            String defaultAnswerValue = defaultAnswer.get();
            if (message != null && TextUtils.containsPlaceholder(message, Placeholder.DEFAULT_ANSWER)) {
                message = TextUtils.replacePlaceholders(message, new HashMap<Placeholder, String>() {
                    {
                        put(Placeholder.DEFAULT_ANSWER, defaultAnswerValue);
                    }
                });
            } else {
                message = String.format("%s [%s]", getMessage(), defaultAnswer.get()); 
            }
        }
        ok(message);
    }

}
