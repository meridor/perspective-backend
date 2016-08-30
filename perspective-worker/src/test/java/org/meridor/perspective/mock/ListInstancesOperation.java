package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    @Autowired
    private InstancesStorage instances;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        consumer.accept(instances);
        return true;
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Set<Instance>> consumer) {
        Set<Instance> matchingInstances = instances.stream()
                .filter(i -> ids.contains(i.getRealId()))
                .collect(Collectors.toSet());
        consumer.accept(matchingInstances);
        return true;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_INSTANCES};
    }
}
