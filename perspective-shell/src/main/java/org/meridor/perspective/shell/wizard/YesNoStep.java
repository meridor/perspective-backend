package org.meridor.perspective.shell.wizard;

import org.meridor.perspective.shell.misc.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public abstract class YesNoStep extends AbstractStep {
    
    @Autowired
    private Logger logger; 
    
    private String yes;
    
    private final boolean proceedAnyway;
    
    private final boolean proceedOnYes;
    

    public YesNoStep(boolean proceedAnyway, boolean proceedOnYes) {
        this.proceedAnyway = proceedAnyway;
        this.proceedOnYes = proceedOnYes;
    }

    public YesNoStep(boolean proceedAnyway) {
        this(proceedAnyway, true);
    }
    
    public YesNoStep() {
        this(false, true);
    }

    @Override
    public boolean run() {
        printMessageWithDefaultAnswer();
        String answer = waitForAnswer();
        while (!validateAnswer(answer)) {
            logger.warn("Answer should be y or n. Please try again or type q to quit:");
            answer = waitForAnswer();
        }
        if (isExitKey(answer)) {
            return false;
        }
        yes = answer;
        return proceedAnyway || (proceedOnYes && isYesKey(yes));
    }
    
    private boolean validateAnswer(String answer) {
        return isYesKey(answer) || isNoKey(answer) || isExitKey(answer);
    }

    @Override
    public String getAnswer() {
        return yes;
    }
}
