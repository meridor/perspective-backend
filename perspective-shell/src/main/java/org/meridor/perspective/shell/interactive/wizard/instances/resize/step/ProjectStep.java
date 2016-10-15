package org.meridor.perspective.shell.interactive.wizard.instances.resize.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractProjectStep;
import org.springframework.stereotype.Component;

@Component("resizeInstancesProjectStep")
public class ProjectStep extends AbstractProjectStep {
    
    @Override
    public String getMessage() {
        return "Select project to resize instances in:";
    }
    
}
