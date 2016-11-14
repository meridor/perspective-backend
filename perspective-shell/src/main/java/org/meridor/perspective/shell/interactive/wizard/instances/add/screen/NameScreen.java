package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.NameStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("instanceNameScreen")
public class NameScreen implements WizardScreen {

    private final NameStep nameStep;

    private final CountOrRangeScreen countOrRangeScreen;

    @Autowired
    public NameScreen(NameStep nameStep, CountOrRangeScreen countOrRangeScreen) {
        this.nameStep = nameStep;
        this.countOrRangeScreen = countOrRangeScreen;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return nameStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.of(countOrRangeScreen);
    }
    
}
