package org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractImageStep;
import org.springframework.stereotype.Component;

@Component("rebuildInstancesImageStep")
public class ImageStep extends AbstractImageStep {

    @Override
    public String getMessage() {
        return "Select new image for instances:";
    }

}
