package org.meridor.perspective.shell.interactive.wizard;

import jline.console.ConsoleReader;
import org.meridor.perspective.shell.common.misc.Logger;
import org.meridor.perspective.shell.common.repository.impl.Placeholder;
import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Component
public abstract class AbstractStep implements Step {

    @Autowired
    private Logger logger;
    
    protected String waitForAnswer() {
        try {
            ConsoleReader consoleReader = new ConsoleReader();
            String answer = consoleReader.readLine();
            Optional<String> defaultAnswer = getDefaultAnswer();
            return answer.isEmpty() && defaultAnswer.isPresent() ?
                    defaultAnswer.get() :
                    answer;
        } catch (IOException e) {
           logger.error(String.format("Error while getting input: %s", e.getMessage()));
            return "";
        }
    }

    protected void printMessageWithDefaultAnswer() {
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
        logger.ok(message);
    }

}
