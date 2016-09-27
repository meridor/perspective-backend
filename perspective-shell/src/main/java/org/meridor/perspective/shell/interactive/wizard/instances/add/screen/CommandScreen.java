package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.CommandStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("addInstancesCommandScreen")
public class CommandScreen implements WizardScreen {

    private final CommandStep commandStep;

    @Autowired
    public CommandScreen(CommandStep commandStep) {
        this.commandStep = commandStep;
    }
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return commandStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.empty();
    }
}
