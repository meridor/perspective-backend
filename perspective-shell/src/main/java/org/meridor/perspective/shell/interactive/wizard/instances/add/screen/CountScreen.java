package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.CountStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("addInstancesCountScreen")
public class CountScreen implements WizardScreen {

    private final CountStep countStep;

    private final ImageScreen imageScreen;

    @Autowired
    public CountScreen(CountStep countStep, ImageScreen imageScreen) {
        this.countStep = countStep;
        this.imageScreen = imageScreen;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return countStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.of(imageScreen);
    }
}
