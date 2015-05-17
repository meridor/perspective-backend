package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;
import static org.meridor.perspective.mock.EntityGenerator.getInstance;

@Component
@Operation(cloud = MOCK, type = LIST_INSTANCES)
public class ListInstancesOperation {
    
    @EntryPoint
    public boolean listInstances(List<Instance> instances) {
        instances.add(getInstance());
        return true;
    }
    
}
