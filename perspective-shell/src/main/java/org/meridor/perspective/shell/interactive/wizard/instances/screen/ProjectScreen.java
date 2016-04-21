package org.meridor.perspective.shell.interactive.wizard.instances.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ProjectScreen implements WizardScreen {
    
    @Autowired
    private ProjectStep projectStep;
    
    @Autowired
    private NameScreen nameScreen;
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return projectStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(nameScreen);
    }
}
