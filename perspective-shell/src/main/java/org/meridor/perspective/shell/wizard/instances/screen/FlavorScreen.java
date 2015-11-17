package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.FlavorStep;
import org.meridor.perspective.shell.wizard.instances.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class FlavorScreen implements WizardScreen {
    
    @Autowired
    private FlavorStep flavorStep;
    
    @Autowired
    private NetworkScreen networkScreen;
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        flavorStep.setProjectName(projectName);
        return flavorStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(networkScreen);
    }
}
