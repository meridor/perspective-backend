package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlavorStep extends SingleChoiceStep {
    
    @Autowired
    private ProjectsRepository projectsRepository;

    @Required
    private String projectName;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    protected List<String> getPossibleChoices() {
        return projectsRepository.showProjects(new ShowProjectsQuery().withNames(projectName)).stream()
                .flatMap(p -> p.getFlavors().stream())
                .map(Flavor::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select flavor to use for instances:";
    }
    
}
