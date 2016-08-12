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

import static org.meridor.perspective.config.OperationType.*;

@Component
public class UpdateInstanceOperation implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateInstanceOperation.class);

    @Autowired
    private InstancesStorage instances;

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        LOG.debug("Updating instance {}", instance);
        return !instances.add(instance); // We expect that instance is already present in the set
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{
                MIGRATE_INSTANCE,
                REBOOT_INSTANCE,
                REBUILD_INSTANCE,
                PAUSE_INSTANCE,
                RESIZE_INSTANCE,
                RESUME_INSTANCE,
                HARD_REBOOT_INSTANCE,
                SHUTDOWN_INSTANCE
        };
    }
}
