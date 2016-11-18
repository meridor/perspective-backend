package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.NetworkStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("addInstancesNetworkScreen")
public class NetworkScreen implements WizardScreen {
    
    @Autowired
    private NetworkStep networkStep;
    
    @Autowired
    private KeypairScreen keypairScreen;
    
    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return networkStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.of(keypairScreen);
    }
}
