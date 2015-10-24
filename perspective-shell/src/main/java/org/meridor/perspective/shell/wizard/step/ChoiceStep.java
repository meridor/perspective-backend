package org.meridor.perspective.shell.wizard.step;

import org.meridor.perspective.shell.wizard.Step;

import java.util.Set;

import static org.meridor.perspective.shell.misc.LoggingUtils.warn;
import static org.meridor.perspective.shell.repository.impl.TextUtils.enumerateValues;
import static org.meridor.perspective.shell.repository.impl.TextUtils.isExitKey;

public abstract class ChoiceStep implements Step {

    private String answer;
    
    @Override
    public boolean run() {
        printMessageWithDefaultAnswer();
        String answer = waitForInput();
        while (!validateAnswer(answer)) {
            warn(String.format("Answer should be one of [%s]. Please try again or type q to quit:", enumerateValues(getPossibleChoices())));
            answer = waitForInput();
        }
        if (isExitKey(answer)) {
            return false;
        }
        this.answer = answer;
        return true;
    }

    @Override
    public String getAnswer() {
        return answer;
    }
    
    private boolean validateAnswer(String answer) {
        return getPossibleChoices().contains(answer) || isExitKey(answer);
    }
    
    protected abstract Set<String> getPossibleChoices();
}
