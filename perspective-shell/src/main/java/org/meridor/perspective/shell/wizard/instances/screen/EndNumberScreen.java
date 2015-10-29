package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.EndNumberStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class EndNumberScreen implements WizardScreen {
    
    @Autowired
    private EndNumberStep endNumberStep;
    
    @Override
    public Step getStep() {
        return endNumberStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return null;
    }
}
