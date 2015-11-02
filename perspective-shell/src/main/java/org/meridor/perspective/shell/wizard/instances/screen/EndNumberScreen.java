package org.meridor.perspective.shell.wizard.instances.screen;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.step.EndNumberStep;
import org.meridor.perspective.shell.wizard.instances.step.StartNumberStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class EndNumberScreen implements WizardScreen {
    
    @Autowired
    private EndNumberStep endNumberStep;

    @Autowired
    private ImageScreen imageScreen;
    
    @Override
    public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
        String from = previousAnswers.get(StartNumberStep.class);
        endNumberStep.setFrom(Integer.parseUnsignedInt(from));
        return endNumberStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
        return Optional.of(imageScreen);
    }
}
