package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.common.misc.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.*;

@Component
public abstract class YesNoStep extends AbstractStep {
    
    @Autowired
    private Logger logger; 
    
    private String yes;

    @Override
    public boolean run() {
        yes = null;
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
        return anyAnswerIsCorrect() || isYesKey(yes);
    }

    protected boolean anyAnswerIsCorrect(){
        return false;
    }
    
    private boolean validateAnswer(String answer) {
        return isYesKey(answer) || isNoKey(answer) || isExitKey(answer);
    }

    @Override
    public String getAnswer() {
        return yes;
    }
}
