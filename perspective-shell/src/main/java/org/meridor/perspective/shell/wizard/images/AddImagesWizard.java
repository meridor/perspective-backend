package org.meridor.perspective.shell.wizard.images;

import org.meridor.perspective.shell.commands.CommandArgument;
import org.meridor.perspective.shell.wizard.BaseWizard;
import org.meridor.perspective.shell.wizard.CommandBuilder;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.images.screen.InstanceScreen;
import org.meridor.perspective.shell.wizard.images.step.InstanceStep;
import org.meridor.perspective.shell.wizard.images.step.NameStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddImagesWizard extends BaseWizard {
    
    @Autowired
    private InstanceScreen instanceScreen;
    
    @Override
    protected WizardScreen getFirstScreen() {
        return instanceScreen;
    }

    @Override
    protected String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("add images");
        if (getAnswers().containsKey(InstanceStep.class)) {
            commandBuilder.addArgument(CommandArgument.INSTANCES, getAnswers().get(InstanceStep.class));
        }
        if (getAnswers().containsKey(NameStep.class)) {
            commandBuilder.addArgument(CommandArgument.NAME, getAnswers().get(NameStep.class));
        }
        return commandBuilder.getCommand();
    }
}
