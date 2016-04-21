package org.meridor.perspective.shell.interactive.wizard.instances.step;

import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.request.FindKeypairsRequest;
import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.result.FindKeypairsResult;
import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KeypairStep extends SingleChoiceStep {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private RequestProvider requestProvider;

    @Required
    private String projectName;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    protected List<String> getPossibleChoices() {
        return projectsRepository.findKeypairs(requestProvider.get(FindKeypairsRequest.class).withProjects(projectName)).stream()
                .map(FindKeypairsResult::getName)
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
