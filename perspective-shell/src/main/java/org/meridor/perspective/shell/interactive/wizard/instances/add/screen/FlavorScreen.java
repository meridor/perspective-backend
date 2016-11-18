package org.meridor.perspective.shell.interactive.wizard.instances.add.screen;

import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.interactive.wizard.AnswersStorage;
import org.meridor.perspective.shell.interactive.wizard.Step;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.FlavorStep;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.interactive.wizard.AnswersStorage.AnswersStorageKey.PROJECT;

@Component("addInstancesFlavorScreen")
public class FlavorScreen implements WizardScreen {

    private final FlavorStep flavorStep;

    private final NetworkScreen networkScreen;

    private final KeypairScreen keypairScreen;
    
    @Autowired
    public FlavorScreen(NetworkScreen networkScreen, FlavorStep flavorStep, KeypairScreen keypairScreen) {
        this.networkScreen = networkScreen;
        this.flavorStep = flavorStep;
        this.keypairScreen = keypairScreen;
    }

    @Override
    public Step getStep(AnswersStorage previousAnswers) {
        return flavorStep;
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
                case OPENSTACK: return Optional.of(networkScreen);
                case DIGITAL_OCEAN: return Optional.of(keypairScreen);
            }
        }
        return Optional.empty();
    }
}
