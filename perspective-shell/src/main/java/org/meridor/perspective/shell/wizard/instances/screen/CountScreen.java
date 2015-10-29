package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.CountStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class CountScreen implements WizardScreen {
    
    @Autowired
    private CountStep countStep;
    
    @Autowired
    private ImageScreen imageScreen;
    
    @Override
    public Step getStep() {
        return countStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(imageScreen);
    }
}
