package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;

@Component
public class AddInstanceOperation implements ProcessingOperation<Instance, Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AddInstanceOperation.class);

    @Autowired
    private InstancesStorage instances;

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        if (instances.add(instance)) {
            LOG.debug("Launched instance {} ({})", instance.getName(), instance.getId());
            return instance;
        } else {
            LOG.debug("Failed to launch instance {} ({})", instance.getName(), instance.getId());
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_INSTANCE};
    }
}
