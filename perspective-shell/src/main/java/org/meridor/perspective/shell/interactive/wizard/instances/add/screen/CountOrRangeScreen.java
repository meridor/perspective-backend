package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.CountOrRangeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("addInstancesCountOrRangeScreen")
public class CountOrRangeScreen implements WizardScreen {

    private final CountOrRangeStep countOrRangeStep;

    private final RangeScreen rangeScreen;

    private final CountScreen countScreen;

    @Autowired
    public CountOrRangeScreen(CountOrRangeStep countOrRangeStep, CountScreen countScreen, RangeScreen rangeScreen) {
        this.countOrRangeStep = countOrRangeStep;
        this.countScreen = countScreen;
        this.rangeScreen = rangeScreen;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return countOrRangeStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        String countOrNumberAnswer = previousAnswers.getAnswer(CountOrRangeStep.class);
        return TextUtils.isYesKey(countOrNumberAnswer) ?
                Optional.of(rangeScreen) : Optional.of(countScreen);
    }
}
