package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.wizard.ChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlavorStep extends ChoiceStep {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Override
    protected List<String> getPossibleChoices() {
        //TODO: use selected project
        return projectsRepository.showProjects(new ShowProjectsQuery()).stream()
                .flatMap(p -> p.getFlavors().stream())
                .map(Flavor::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select flavor to use for instances:";
    }
    
}
