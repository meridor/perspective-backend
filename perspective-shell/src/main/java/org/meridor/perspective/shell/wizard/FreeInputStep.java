package org.meridor.perspective.shell.wizard;

import org.meridor.perspective.shell.validator.ObjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.meridor.perspective.shell.misc.LoggingUtils.warn;
import static org.meridor.perspective.shell.repository.impl.TextUtils.isExitKey;
import static org.meridor.perspective.shell.repository.impl.TextUtils.joinLines;

@Component
public abstract class FreeInputStep implements Step {

    private String answer;

    @Autowired
    private ObjectValidator objectValidator;

    @Override
    public boolean run() {
        printMessageWithDefaultAnswer();
        String answer = waitForAnswer();
        saveAnswerToFields(answer);
        while (!validateAnswerAndShowErrors()) {
            answer = waitForAnswer();
        }
        if (isExitKey(answer)) {
            return false;
        }
        this.answer = answer;
        return true;
    }

    private boolean validateAnswerAndShowErrors() {
        Set<String> errors = objectValidator.validate(this);
        warn(String.format("Invalid data provided: %s\n Please try again or type q to quit:", joinLines(errors)));
        return errors.size() == 0;
    }

    protected abstract void saveAnswerToFields(String answer);
    
    @Override
    public String getAnswer() {
        return answer;
    }
}
