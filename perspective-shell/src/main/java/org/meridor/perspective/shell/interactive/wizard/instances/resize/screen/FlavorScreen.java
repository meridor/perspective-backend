package org.meridor.perspective.shell.interactive.wizard.instances.resize.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.FlavorStep;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("resizeInstancesFlavorScreen")
public class FlavorScreen implements WizardScreen {

    private final FlavorStep flavorStep;

    @Autowired
    public FlavorScreen(FlavorStep flavorStep) {
        this.flavorStep = flavorStep;
    }

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        flavorStep.setProjectName(projectName);
        return flavorStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.empty();
    }
}
