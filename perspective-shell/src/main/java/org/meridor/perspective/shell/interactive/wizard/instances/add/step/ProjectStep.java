package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.FindProjectsRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.interactive.wizard.SingleChoiceStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("addInstancesProjectStep")
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
