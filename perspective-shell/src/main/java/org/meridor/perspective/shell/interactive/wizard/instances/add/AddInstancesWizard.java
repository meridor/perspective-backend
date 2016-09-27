package org.meridor.perspective.shell.interactive.wizard.instances.add;

import org.meridor.perspective.shell.interactive.wizard.BaseWizard;
import org.meridor.perspective.shell.interactive.wizard.CommandBuilder;
import org.meridor.perspective.shell.interactive.wizard.WizardScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.screen.ProjectScreen;
import org.meridor.perspective.shell.interactive.wizard.instances.add.step.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.interactive.commands.CommandArgument.*;

@Component
public class AddInstancesWizard extends BaseWizard {
    
    @Autowired
    private ProjectScreen projectScreen;
    
    @Override
    protected WizardScreen getFirstScreen() {
        return projectScreen;
    }

    @Override
    public String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("add instances");
        Optional<String> projectCandidate = getAnswer(ProjectStep.class);
        if (projectCandidate.isPresent()) {
            commandBuilder.addArgument(PROJECT, projectCandidate.get());
        }
        Optional<String> nameCandidate = getAnswer(NameStep.class);
        if (nameCandidate.isPresent()) {
            commandBuilder.addArgument(NAME, nameCandidate.get());
        }
        Optional<String> flavorCandidate = getAnswer(FlavorStep.class);
        if (flavorCandidate.isPresent()) {
            commandBuilder.addArgument(FLAVOR, flavorCandidate.get());
        }
        Optional<String> imageCandidate = getAnswer(ImageStep.class);
        if (imageCandidate.isPresent()) {
            commandBuilder.addArgument(IMAGE, imageCandidate.get());
        }
        Optional<String> networkCandidate = getAnswer(NetworkStep.class);
        if (networkCandidate.isPresent()) {
            commandBuilder.addArgument(NETWORK, networkCandidate.get());
        }
        Optional<String> keypairCandidate = getAnswer(KeypairStep.class);
        if (keypairCandidate.isPresent()) {
            commandBuilder.addArgument(KEYPAIR, keypairCandidate.get());
        }
        Optional<String> countCandidate = getAnswer(CountStep.class);
        Optional<String> rangeCandidate = getAnswer(RangeStep.class);
        if (countCandidate.isPresent()) {
            commandBuilder.addArgument(COUNT, countCandidate.get());
        } else if (rangeCandidate.isPresent()) {
            commandBuilder.addArgument(RANGE, rangeCandidate.get());
        }

        Map<String, Set<String>> options = new HashMap<>();
        Optional<String> commandCandidate = getAnswer(CommandStep.class);
        if (commandCandidate.isPresent()) {
            options.put(COMMAND.toString(), new HashSet<>(Arrays.asList(new String[]{commandCandidate.get()})));
        }
        if (!options.isEmpty()) {
            commandBuilder.addArgument(OPTIONS, options);
        }
        return commandBuilder.getCommand();
    }

}
