package org.meridor.perspective.shell.interactive.wizard.instances.resize.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.InstanceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("resizeInstancesInstanceScreen")
public class InstanceScreen implements WizardScreen {

    private final InstanceStep instanceStep;

    private final FlavorScreen flavorScreen;

    @Autowired
    public InstanceScreen(InstanceStep instanceStep, FlavorScreen flavorScreen) {
        this.instanceStep = instanceStep;
        this.flavorScreen = flavorScreen;
    }

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return instanceStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(flavorScreen);
    }

}
