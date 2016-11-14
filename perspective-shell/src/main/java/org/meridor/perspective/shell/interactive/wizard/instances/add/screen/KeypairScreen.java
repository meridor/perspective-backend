package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.KeypairStep;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("addInstancesKeypairScreen")
public class KeypairScreen implements WizardScreen {

    private final KeypairStep keypairStep;

    @Autowired
    public KeypairScreen(KeypairStep keypairStep) {
        this.keypairStep = keypairStep;
    }
    
    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        String projectName = previousAnswers.getAnswer(ProjectStep.class);
        keypairStep.setProjectName(projectName);
        return keypairStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.empty();
    }
}
