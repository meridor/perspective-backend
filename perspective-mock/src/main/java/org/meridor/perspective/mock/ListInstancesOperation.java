package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
@Operation(cloud = MOCK, type = LIST_INSTANCES)
public class ListInstancesOperation {
    
    @Autowired
    private InstancesStorage instances;
    
    @EntryPoint
    public boolean listInstances(Cloud cloud, Consumer<Set<Instance>> consumer) {
        consumer.accept(instances);
        return true;
    }
    
}
