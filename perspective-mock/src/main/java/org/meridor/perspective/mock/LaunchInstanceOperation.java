package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.LAUNCH_INSTANCE;

@Component
public class LaunchInstanceOperation implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(LaunchInstanceOperation.class);

    @Autowired
    private InstancesStorage instances;

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        LOG.debug("Launching instance {}", instance);
        return instances.add(instance);
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LAUNCH_INSTANCE};
    }
}
