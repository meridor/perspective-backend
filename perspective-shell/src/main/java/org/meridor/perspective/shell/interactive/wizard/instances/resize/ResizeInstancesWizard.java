package org.meridor.perspective.shell.interactive.wizard.instances.resize;

import org.meridor.perspective.shell.interactive.wizard.BaseWizard;
import org.meridor.perspective.shell.interactive.wizard.CommandBuilder;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.screen.ProjectScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.FlavorStep;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.InstanceStep;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.interactive.commands.CommandArgument.*;

@Component
public class ResizeInstancesWizard extends BaseWizard {

    private final ProjectScreen projectScreen;

    @Autowired
    public ResizeInstancesWizard(ProjectScreen projectScreen) {
        this.projectScreen = projectScreen;
    }

    @Override
    protected WizardScreen getFirstScreen() {
        return projectScreen;
    }

    @Override
    public String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("resize");

        Optional<String> projectCandidate = getAnswer(ProjectStep.class);
        if (projectCandidate.isPresent()) {
            commandBuilder.addArgument(PROJECT, projectCandidate.get());
        }
        Optional<String> instancesCandidate = getAnswer(InstanceStep.class);
        if (instancesCandidate.isPresent()) {
            commandBuilder.addArgument(INSTANCES, instancesCandidate.get());
        }
        Optional<String> flavorCandidate = getAnswer(FlavorStep.class);
        if (flavorCandidate.isPresent()) {
            commandBuilder.addArgument(FLAVOR, flavorCandidate.get());
        }

        return commandBuilder.getCommand();
    }
}
