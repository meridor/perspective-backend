package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

@Component
public class CommandStep extends FreeInputStep {
    
    @Required
    private String command;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        this.command = answer;
    }

    @Override
    public String getMessage() {
        return "Select command to launch container with.";
    }
}
