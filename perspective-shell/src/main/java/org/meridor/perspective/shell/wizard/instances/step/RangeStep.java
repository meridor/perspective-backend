package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

@Component
public class RangeStep extends FreeInputStep {
    
    @Required
    private String range;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        this.range = answer;
    }

    @Override
    public String getMessage() {
        return "Specify instance numbers range (e.g. 2-4,5-7,15):";
    }
}
