package org.meridor.perspective.shell.interactive.wizard.instances.rebuild;

import org.meridor.perspective.shell.interactive.wizard.BaseWizard;
import org.meridor.perspective.shell.interactive.wizard.CommandBuilder;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.screen.ProjectScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.ImageStep;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.InstanceStep;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.ProjectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.interactive.commands.CommandArgument.*;

@Component
public class RebuildInstancesWizard extends BaseWizard {

    private final ProjectScreen projectScreen;

    @Autowired
    public RebuildInstancesWizard(ProjectScreen projectScreen) {
        this.projectScreen = projectScreen;
    }

    @Override
    protected WizardScreen getFirstScreen() {
        return projectScreen;
    }

    @Override
    public String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("rebuild");

        Optional<String> projectCandidate = getAnswer(ProjectStep.class);
        projectCandidate.ifPresent(pc -> commandBuilder.addArgument(PROJECT, pc));
        Optional<String> instancesCandidate = getAnswer(InstanceStep.class);
        instancesCandidate.ifPresent(ic -> commandBuilder.addArgument(INSTANCES, ic));
        Optional<String> imageCandidate = getAnswer(ImageStep.class);
        imageCandidate.ifPresent(fc -> commandBuilder.addArgument(IMAGE, fc));

        return commandBuilder.getCommand();
    }
}
