package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.RangeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class RangeScreen implements WizardScreen {
    
    @Autowired
    private RangeStep rangeStep;

    @Autowired
    private ImageScreen imageScreen;

    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return rangeStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(imageScreen);
    }
}
