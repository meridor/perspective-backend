package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.shell.query.QueryProvider;
import org.meridor.perspective.shell.query.ShowKeypairsQuery;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class KeypairStep extends SingleChoiceStep {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private QueryProvider queryProvider;

    @Required
    private String projectName;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    protected List<String> getPossibleChoices() {
        return projectsRepository.showKeypairs(queryProvider.get(ShowKeypairsQuery.class).withProjects(projectName)).values().stream()
                .flatMap(Collection::stream)
                .map(Keypair::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select keypair to use for instances:";
    }

    @Override
    public boolean answerRequired() {
        return false;
    }
}
