package org.meridor.perspective.shell.interactive.wizard.instances.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.step.CommandStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
@Component
public class CommandScreen implements WizardScreen {
    
    @Autowired
    private CommandStep commandStep;
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return commandStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.empty();
    }
}
