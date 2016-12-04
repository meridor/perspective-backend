package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractFlavorStep;
import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractProjectStep;
import org.springframework.stereotype.Component;

@Component("addInstancesFlavorStep")
public class FlavorStep extends AbstractFlavorStep {
    
    @Override
    public String getMessage() {
        return "Select flavor to use for instances:";
    }

    @Override
    protected Class<? extends AbstractProjectStep> getProjectStepClass() {
        return ProjectStep.class;
    }
}
