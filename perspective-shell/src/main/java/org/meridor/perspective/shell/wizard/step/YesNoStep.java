package org.meridor.perspective.shell.wizard.step;

import org.meridor.perspective.shell.wizard.Step;

import static org.meridor.perspective.shell.misc.LoggingUtils.warn;
import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

public abstract class YesNoStep implements Step {
    
    private String yes;
    
    private final boolean proceedOnYes;

    public YesNoStep(boolean proceedOnYes) {
        this.proceedOnYes = proceedOnYes;
    }

    public YesNoStep() {
        this(true);
    }

    @Override
    public boolean run() {
        printMessageWithDefaultAnswer();
        String answer = waitForInput();
        while (!validateAnswer(answer)) {
            warn("Answer should be y or n. Please try again or type q to quit:");
            answer = waitForInput();
        }
        if (isExitKey(answer)) {
            return false;
        }
        yes = answer;
        return proceedOnYes && isYesKey(yes);
    }
    
    private boolean validateAnswer(String answer) {
        return isYesKey(answer) || isNoKey(answer);
    }

    @Override
    public String getAnswer() {
        return yes;
    }
}
