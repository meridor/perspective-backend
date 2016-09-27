package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.RangeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("addInstancesRangeScreen")
public class RangeScreen implements WizardScreen {

    private final RangeStep rangeStep;

    private final ImageScreen imageScreen;

    @Autowired
    public RangeScreen(RangeStep rangeStep, ImageScreen imageScreen) {
        this.rangeStep = rangeStep;
        this.imageScreen = imageScreen;
    }

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return rangeStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(imageScreen);
    }
}
