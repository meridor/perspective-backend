package org.meridor.perspective.shell.wizard.instances;

import org.meridor.perspective.shell.wizard.BaseWizard;
import org.meridor.perspective.shell.wizard.CommandBuilder;
import org.meridor.perspective.shell.wizard.WizardScreen;
import org.meridor.perspective.shell.wizard.instances.screen.ProjectScreen;
import org.meridor.perspective.shell.wizard.instances.step.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.commands.CommandArgument.*;

@Component
public class AddInstancesWizard extends BaseWizard {
    
    @Autowired
    private ProjectScreen projectScreen;
    
    @Override
    protected WizardScreen getFirstScreen() {
        return projectScreen;
    }

    @Override
    protected String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("add instances");
        if (getAnswers().containsKey(ProjectStep.class)) {
            commandBuilder.addArgument(PROJECT, getAnswers().get(ProjectStep.class));
        }
        if (getAnswers().containsKey(NameStep.class)) {
            commandBuilder.addArgument(NAME, getAnswers().get(NameStep.class));
        }
        if (getAnswers().containsKey(FlavorStep.class)) {
            commandBuilder.addArgument(FLAVOR, getAnswers().get(FlavorStep.class));
        }
        if (getAnswers().containsKey(ImageStep.class)) {
            commandBuilder.addArgument(IMAGE, getAnswers().get(ImageStep.class));
        }
        if (getAnswers().containsKey(NetworkStep.class)) {
            commandBuilder.addArgument(NETWORK, getAnswers().get(NetworkStep.class));
        }
        if (getAnswers().containsKey(KeypairStep.class)) {
            commandBuilder.addArgument(KEYPAIR, getAnswers().get(KeypairStep.class));
        }
        if (getAnswers().containsKey(CountStep.class)) {
            commandBuilder.addArgument(COUNT, getAnswers().get(CountStep.class));
        } else if (getAnswers().containsKey(RangeStep.class)) {
            commandBuilder.addArgument(RANGE, getAnswers().get(RangeStep.class));
        }

        Map<String, Set<String>> options = new HashMap<>();
        if (getAnswers().containsKey(CommandStep.class)) {
            options.put(COMMAND.toString(), new HashSet<>(Arrays.asList(new String[]{getAnswers().get(CommandStep.class)})));
        }
        if (!options.isEmpty()) {
            commandBuilder.addArgument(OPTIONS, options);
        }
        return commandBuilder.getCommand();
    }

}
