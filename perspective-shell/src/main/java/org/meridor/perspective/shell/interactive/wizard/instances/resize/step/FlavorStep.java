package org.meridor.perspective.shell.interactive.wizard.instances.resize.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractFlavorStep;
import org.springframework.stereotype.Component;

@Component("resizeInstancesFlavorStep")
public class FlavorStep extends AbstractFlavorStep {

    @Override
    public String getMessage() {
        return "Select new flavor for instances:";
    }

}
