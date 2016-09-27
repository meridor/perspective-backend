package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("addInstancesProjectScreen")
public class ProjectScreen implements WizardScreen {

    private final ProjectStep projectStep;

    private final NameScreen nameScreen;

    @Autowired
    public ProjectScreen(NameScreen nameScreen, ProjectStep projectStep) {
        this.nameScreen = nameScreen;
        this.projectStep = projectStep;
    }

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return projectStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(nameScreen);
    }
}
