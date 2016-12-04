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
        projectCandidate.ifPresent(pc -> commandBuilder.addArgument(PROJECT, pc));
        Optional<String> instancesCandidate = getAnswer(InstanceStep.class);
        instancesCandidate.ifPresent(ic -> commandBuilder.addArgument(INSTANCES, ic));
        Optional<String> flavorCandidate = getAnswer(FlavorStep.class);
        flavorCandidate.ifPresent(fc -> commandBuilder.addArgument(FLAVOR, fc));

        return commandBuilder.getCommand();
    }
}
