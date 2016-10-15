package org.meridor.perspective.shell.interactive.wizard.instances.rebuild.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.ImageStep;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("rebuildInstancesImageScreen")
public class ImageScreen implements WizardScreen {

    private final ImageStep imageStep;

    @Autowired
    public ImageScreen(ImageStep imageStep) {
        this.imageStep = imageStep;
    }

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        imageStep.setProjectName(projectName);
        return imageStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.empty();
    }
}
