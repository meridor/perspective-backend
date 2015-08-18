package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.*;

@Component
@Operation(cloud = MOCK, type = {
        MIGRATE_INSTANCE,
        REBOOT_INSTANCE,
        REBUILD_INSTANCE,
        PAUSE_INSTANCE,
        RESIZE_INSTANCE,
        RESUME_INSTANCE,
        HARD_REBOOT_INSTANCE,
        SHUTDOWN_INSTANCE,
        SNAPSHOT_INSTANCE
})
public class UpdateInstanceOperation {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateInstanceOperation.class);
    
    @Autowired
    private InstancesStorage instances;
    
    @EntryPoint
    public boolean updateInstances(Cloud cloud, Supplier<Instance> supplier) throws InterruptedException {
        Instance instance = supplier.get();
        LOG.debug("Updating instance {}", instance);
        return !instances.add(instance); // We expect that instance is already present in the set
    }
    
}
