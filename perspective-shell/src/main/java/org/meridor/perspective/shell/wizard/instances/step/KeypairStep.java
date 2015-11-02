package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.wizard.ChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KeypairStep extends ChoiceStep {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Override
    protected List<String> getPossibleChoices() {
        return projectsRepository.showProjects(new ShowProjectsQuery()).stream()
                .flatMap(p -> p.getKeypairs().stream())
                .map(Keypair::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select keypair to use for instances:";
    }
    
}
