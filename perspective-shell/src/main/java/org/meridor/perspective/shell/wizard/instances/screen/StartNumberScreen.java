package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.StartNumberStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class StartNumberScreen implements WizardScreen {
    
    @Autowired
    private StartNumberStep startNumberStep;
    
    @Autowired
    private EndNumberScreen endNumberScreen;
    
    @Override
    public Step getStep() {
        return startNumberStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(endNumberScreen);
    }
}
