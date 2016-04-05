package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.request.RequestProvider;
import org.meridor.perspective.shell.request.FindProjectsRequest;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.result.FindProjectsResult;
import org.meridor.perspective.shell.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectStep extends SingleChoiceStep {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private RequestProvider requestProvider;
    
    @Override
    protected List<String> getPossibleChoices() {
        return projectsRepository.findProjects(requestProvider.get(FindProjectsRequest.class)).stream()
                .map(FindProjectsResult::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select project to launch instances in:";
    }
    
}
