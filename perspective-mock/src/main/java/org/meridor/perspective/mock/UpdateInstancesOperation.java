package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.*;

@Component
@Operation(cloud = MOCK, type = {DELETE_INSTANCE, LAUNCH_INSTANCE, REBOOT_INSTANCE, HARD_REBOOT_INSTANCE, SNAPSHOT_INSTANCE})
public class UpdateInstancesOperation {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateInstancesOperation.class);
    
    @EntryPoint
    public boolean updateInstances(Supplier<List<Instance>> supplier) {
        LOG.debug("Updating {} instances", supplier.get().size());
        return true;
    }
    
}
