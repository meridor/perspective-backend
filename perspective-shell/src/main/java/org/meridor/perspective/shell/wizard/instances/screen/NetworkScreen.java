package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.NetworkStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class NetworkScreen implements WizardScreen {
    
    @Autowired
    private NetworkStep networkStep;
    
    @Autowired
    private KeypairScreen keypairScreen;
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return networkStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(keypairScreen);
    }
}
