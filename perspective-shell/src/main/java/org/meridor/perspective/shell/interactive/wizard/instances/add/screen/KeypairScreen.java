package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.KeypairStep;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("addInstancesKeypairScreen")
public class KeypairScreen implements WizardScreen {

    private final KeypairStep keypairStep;

    @Autowired
    public KeypairScreen(KeypairStep keypairStep) {
        this.keypairStep = keypairStep;
    }
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        keypairStep.setProjectName(projectName);
        return keypairStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.empty();
    }
}
