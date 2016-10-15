package org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractProjectStep;
import org.springframework.stereotype.Component;

@Component("rebuildInstancesProjectStep")
public class ProjectStep extends AbstractProjectStep {
    
    @Override
    public String getMessage() {
        return "Select project to rebuild instances in:";
    }
    
}
