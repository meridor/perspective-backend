package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.wizard.ChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectStep extends ChoiceStep {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Override
    protected List<String> getPossibleChoices() {
        return projectsRepository.showProjects(new ShowProjectsQuery()).stream()
                .map(Project::getId)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select project to launch instances in.";
    }
    
}
