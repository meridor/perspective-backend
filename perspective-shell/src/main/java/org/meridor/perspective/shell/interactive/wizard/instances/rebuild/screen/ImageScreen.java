package org.meridor.perspective.shell.interactive.wizard.instances.rebuild.screen;

import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.ImageStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("rebuildInstancesImageScreen")
public class ImageScreen implements WizardScreen {

    private final ImageStep imageStep;

    @Autowired
    public ImageScreen(ImageStep imageStep) {
        this.imageStep = imageStep;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return imageStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        return Optional.empty();
    }
}
