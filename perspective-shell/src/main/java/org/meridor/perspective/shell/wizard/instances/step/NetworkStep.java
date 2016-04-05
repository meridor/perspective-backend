package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.request.RequestProvider;
import org.meridor.perspective.shell.request.FindNetworksRequest;
import org.meridor.perspective.shell.result.FindNetworksResult;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NetworkStep extends SingleChoiceStep {
    
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
        return projectsRepository.findNetworks(requestProvider.get(FindNetworksRequest.class).withProjects(projectName)).stream()
                .map(FindNetworksResult::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select network to use for instances:";
    }
    
}
