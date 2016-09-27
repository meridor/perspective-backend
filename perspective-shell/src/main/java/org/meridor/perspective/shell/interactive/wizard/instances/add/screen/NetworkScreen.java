package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.NetworkStep;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("addInstancesNetworkScreen")
public class NetworkScreen implements WizardScreen {
    
    @Autowired
    private NetworkStep networkStep;
    
    @Autowired
    private KeypairScreen keypairScreen;
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String projectName = previousAnswers.get(ProjectStep.class);
        networkStep.setProjectName(projectName);
        return networkStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(keypairScreen);
    }
}
