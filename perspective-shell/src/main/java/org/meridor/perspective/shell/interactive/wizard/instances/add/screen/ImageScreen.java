package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ImageStep;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.PROJECT;

@Component("addInstancesImageScreen")
public class ImageScreen implements WizardScreen {
    
    private final ImageStep imageStep;
    
    private final FlavorScreen flavorScreen;
    
    private final CommandScreen commandScreen;

    @Autowired
    public ImageScreen(ImageStep imageStep, FlavorScreen flavorScreen, CommandScreen commandScreen) {
        this.imageStep = imageStep;
        this.flavorScreen = flavorScreen;
        this.commandScreen = commandScreen;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return imageStep;
    }

    @Override
    public Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers) {
        Optional<FindProjectsResult> projectCandidate = Optional.ofNullable(
                previousAnswers.get(
                        ProjectStep.class,
                        PROJECT,
                        FindProjectsResult.class
                )
        );
        if (projectCandidate.isPresent()) {
            FindProjectsResult project = projectCandidate.get();
            switch (project.getCloudType()) {
                case OPENSTACK:
                case DIGITAL_OCEAN:
                    return Optional.of(flavorScreen);
                case DOCKER: return Optional.of(commandScreen);
            }
        }
        return Optional.empty();
    }
    
}
