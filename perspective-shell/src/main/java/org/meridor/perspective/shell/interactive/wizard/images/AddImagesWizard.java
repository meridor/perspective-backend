package org.meridor.perspective.shell.interactive.wizard.images;

import org.meridor.perspective.shell.interactive.wizard.BaseWizard;
import org.meridor.perspective.shell.interactive.wizard.CommandBuilder;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.images.screen.InstanceScreen;
import org.meridor.perspective.shell.interactive.wizard.images.step.InstanceStep;
import org.meridor.perspective.shell.interactive.wizard.images.step.NameStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.interactive.commands.CommandArgument.INSTANCES;
import static org.meridor.perspective.shell.interactive.commands.CommandArgument.NAME;

@Component
public class AddImagesWizard extends BaseWizard {
    
    @Autowired
    private InstanceScreen instanceScreen;
    
    @Override
    protected WizardScreen getFirstScreen() {
        return instanceScreen;
    }

    @Override
    public String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("add images");
        Optional<String> instancesCandidate = getAnswer(InstanceStep.class);
        instancesCandidate.ifPresent(ic -> commandBuilder.addArgument(INSTANCES, ic));
        
        Optional<String> nameCandidate = getAnswer(NameStep.class);
        nameCandidate.ifPresent(nc -> commandBuilder.addArgument(NAME, nc));
        return commandBuilder.getCommand();
    }
}
