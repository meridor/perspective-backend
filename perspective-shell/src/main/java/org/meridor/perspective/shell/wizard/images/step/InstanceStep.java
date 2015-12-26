package org.meridor.perspective.shell.wizard.images.step;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.query.QueryProvider;
import org.meridor.perspective.shell.query.ShowInstancesQuery;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.wizard.MultipleChoicesStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstanceStep extends MultipleChoicesStep {
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private QueryProvider queryProvider;
    
    @Override
    protected List<String> getPossibleChoices() {
        return instancesRepository.showInstances(queryProvider.get(ShowInstancesQuery.class)).stream()
                .map(Instance::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getMessage() {
        return "Select instances to create images for:";
    }
    
}
