package org.meridor.perspective.shell.interactive.wizard.instances.rebuild.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.InstanceStep;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("rebuildInstancesInstanceScreen")
public class InstanceScreen implements WizardScreen {

    private final InstanceStep instanceStep;

    private final ImageScreen imageScreen;

    @Autowired
    public InstanceScreen(InstanceStep instanceStep, ImageScreen imageScreen) {
        this.instanceStep = instanceStep;
        this.imageScreen = imageScreen;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        String projectName = previousAnswers.getAnswer(ProjectStep.class);
        instanceStep.setProjectName(projectName);
        return instanceStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.of(imageScreen);
    }

}
