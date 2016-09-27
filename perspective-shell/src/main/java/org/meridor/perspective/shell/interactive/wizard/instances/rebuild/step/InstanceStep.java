package org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractMultipleInstancesStep;
import org.springframework.stereotype.Component;

@Component("rebuildInstancesInstanceStep")
public class InstanceStep extends AbstractMultipleInstancesStep {

    @Override
    public String getMessage() {
        return "Select instances to rebuild:";
    }

}
