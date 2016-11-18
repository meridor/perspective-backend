package org.meridor.perspective.shell.interactive.wizard.instances.resize.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.FlavorStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("resizeInstancesFlavorScreen")
public class FlavorScreen implements WizardScreen {

    private final FlavorStep flavorStep;

    @Autowired
    public FlavorScreen(FlavorStep flavorStep) {
        this.flavorStep = flavorStep;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return flavorStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.empty();
    }
}
