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
    
    private final ProjectScreen projectScreen;

    @Autowired
    public AddInstancesWizard(ProjectScreen projectScreen) {
        this.projectScreen = projectScreen;
    }

    @Override
    protected WizardScreen getFirstScreen() {
        return projectScreen;
    }

    @Override
    public String getCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("add instances");
        Optional<String> projectCandidate = getAnswer(ProjectStep.class);
        projectCandidate.ifPresent(pc -> commandBuilder.addArgument(PROJECT, pc));
        Optional<String> nameCandidate = getAnswer(NameStep.class);
        nameCandidate.ifPresent(nc -> commandBuilder.addArgument(NAME, nc));
        Optional<String> flavorCandidate = getAnswer(FlavorStep.class);
        flavorCandidate.ifPresent(fc -> commandBuilder.addArgument(FLAVOR, fc));
        Optional<String> imageCandidate = getAnswer(ImageStep.class);
        imageCandidate.ifPresent(ic -> commandBuilder.addArgument(IMAGE, ic));
        Optional<String> networkCandidate = getAnswer(NetworkStep.class);
        networkCandidate.ifPresent(nc -> commandBuilder.addArgument(NETWORK, nc));
        Optional<String> keypairCandidate = getAnswer(KeypairStep.class);
        keypairCandidate.ifPresent(kc -> commandBuilder.addArgument(KEYPAIR, kc));
        Optional<String> countCandidate = getAnswer(CountStep.class);
        Optional<String> rangeCandidate = getAnswer(RangeStep.class);
        if (countCandidate.isPresent()) {
            commandBuilder.addArgument(COUNT, countCandidate.get());
        } else
            rangeCandidate.ifPresent(rc -> commandBuilder.addArgument(RANGE, rc));

        Map<String, Set<String>> options = new HashMap<>();
        Optional<String> commandCandidate = getAnswer(CommandStep.class);
        commandCandidate.ifPresent(cc -> options.put(COMMAND.toString(), new HashSet<>(Arrays.asList(new String[]{cc}))));
        if (!options.isEmpty()) {
            commandBuilder.addArgument(OPTIONS, options);
        }
        return commandBuilder.getCommand();
    }

}
