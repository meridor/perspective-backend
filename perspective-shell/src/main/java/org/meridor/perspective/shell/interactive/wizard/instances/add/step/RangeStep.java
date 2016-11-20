package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.meridor.perspective.shell.interactive.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

@Component("addInstancesRangeStep")
public class RangeStep extends FreeInputStep {
    
    @Required
    private String range;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        this.range = answer;
    }

    @Override
    public String getMessage() {
        return "Specify instance numbers range (e.g. 2-4,6-8,15):";
    }
}
