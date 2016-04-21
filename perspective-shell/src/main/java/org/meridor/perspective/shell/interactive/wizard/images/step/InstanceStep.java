package org.meridor.perspective.shell.interactive.wizard.images.step;

import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.repository.InstancesRepository;
import org.meridor.perspective.shell.common.result.FindInstancesResult;
import org.meridor.perspective.shell.interactive.wizard.MultipleChoicesStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstanceStep extends MultipleChoicesStep {
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private RequestProvider requestProvider;
    
    @Override
    protected List<String> getPossibleChoices() {
        return instancesRepository.findInstances(requestProvider.get(FindInstancesRequest.class)).stream()
                .map(FindInstancesResult::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select instances to create images for:";
    }
    
}
