package org.meridor.perspective.shell.interactive.wizard.instances.rebuild;

import org.meridor.perspective.shell.interactive.wizard.BaseWizard;
import org.meridor.perspective.shell.interactive.wizard.CommandBuilder;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.screen.InstanceScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.ImageStep;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.step.InstanceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.interactive.commands.CommandArgument.IMAGE;
import static org.meridor.perspective.shell.interactive.commands.CommandArgument.INSTANCES;

@Component
public class RebuildInstancesWizard extends BaseWizard {

    private final InstanceScreen instanceScreen;

    @Autowired
    public RebuildInstancesWizard(InstanceScreen instanceScreen) {
        this.instanceScreen = instanceScreen;
    }

    @Override
    protected WizardScreen getFirstScreen() {
        return instanceScreen;
    }

    @Override
    public String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("rebuild");

        Optional<String> instancesCandidate = getAnswer(InstanceStep.class);
        if (instancesCandidate.isPresent()) {
            commandBuilder.addArgument(INSTANCES, instancesCandidate.get());
        }
        Optional<String> imageCandidate = getAnswer(ImageStep.class);
        if (imageCandidate.isPresent()) {
            commandBuilder.addArgument(IMAGE, imageCandidate.get());
        }

        return commandBuilder.getCommand();
    }
}
