package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.CountOrNumberStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class CountOrNumberScreen implements WizardScreen {
    
    @Autowired
    private CountOrNumberStep countOrNumberStep;
    
    @Autowired
    private StartNumberScreen startNumberScreen;
    
    @Autowired
    private CountScreen countScreen;
    
    @Override
    public Step getStep() {
        return countOrNumberStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        String countOrNumberAnswer = previousAnswers.get(CountOrNumberStep.class);
        return TextUtils.isYesKey(countOrNumberAnswer) ?
                Optional.of(startNumberScreen) : Optional.of(countScreen);
    }
}
