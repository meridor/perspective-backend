package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.NameStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class NameScreen implements WizardScreen {
    
    @Autowired
    private NameStep nameStep;
    
    @Autowired
    private CountOrNumberScreen countOrNumberScreen;
    
    @Override
    public Step getStep() {
        return nameStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(countOrNumberScreen);
    }
    
}
