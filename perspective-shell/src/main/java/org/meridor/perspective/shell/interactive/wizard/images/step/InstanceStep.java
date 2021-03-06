package org.meridor.perspective.shell.interactive.wizard.images.step;

import org.meridor.perspective.shell.interactive.wizard.common.step.AbstractMultipleInstancesStep;
import org.springframework.stereotype.Component;

@Component("addImagesInstanceStep")
public class InstanceStep extends AbstractMultipleInstancesStep {
    
    @Override
    public String getMessage() {
        return "Select instances to create images for:";
    }
    
}
