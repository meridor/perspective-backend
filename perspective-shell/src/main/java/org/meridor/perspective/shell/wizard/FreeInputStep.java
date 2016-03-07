package org.meridor.perspective.shell.wizard;

import org.meridor.perspective.shell.misc.Logger;
import org.meridor.perspective.shell.validator.ObjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.isExitKey;
import static org.meridor.perspective.shell.repository.impl.TextUtils.joinLines;

@Component
public abstract class FreeInputStep extends AbstractStep {

    private String answer;

    @Autowired
    private ObjectValidator objectValidator;
    
    @Autowired
    private Logger logger;

    @Override
    public boolean run() {
        answer = null;
        printMessageWithDefaultAnswer();
        Optional<String> answer = processAnswer();
        if (!answer.isPresent()) {
            return false;
        }
        while (!validateAnswerAndShowErrors()) {
            answer = processAnswer();
            if (!answer.isPresent()) {
                return false;
            }
        }
        this.answer = answer.get();
        return true;
    }
    
    private Optional<String> processAnswer() {
        String answer = waitForAnswer();
        if (isExitKey(answer)) {
            return Optional.empty();
        }
        saveAnswerToFields(answer);
        return Optional.of(answer);
    }

    private boolean validateAnswerAndShowErrors() {
        Set<String> errors = objectValidator.validate(this);
        boolean isValid = (errors.size() == 0);
        if (!isValid) {
            logger.warn(String.format("Invalid data provided: %s\n Please try again or type q to quit:", joinLines(errors)));
        }
        return isValid;
    }

    protected abstract void saveAnswerToFields(String answer);
    
    @Override
    public String getAnswer() {
        return answer;
    }
}
