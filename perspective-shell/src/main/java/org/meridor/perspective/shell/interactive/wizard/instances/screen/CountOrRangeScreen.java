package org.meridor.perspective.shell.interactive.wizard.instances.screen;

import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.step.CountOrRangeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class CountOrRangeScreen implements WizardScreen {
    
    @Autowired
    private CountOrRangeStep countOrNumberStep;
    
    @Autowired
    private RangeScreen rangeScreen;
    
    @Autowired
    private CountScreen countScreen;
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        return countOrNumberStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        String countOrNumberAnswer = previousAnswers.get(CountOrRangeStep.class);
        return TextUtils.isYesKey(countOrNumberAnswer) ?
                Optional.of(rangeScreen) : Optional.of(countScreen);
    }
}
