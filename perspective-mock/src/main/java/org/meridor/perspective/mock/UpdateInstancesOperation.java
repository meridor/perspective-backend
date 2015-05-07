package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.*;

@Component
@Operation(cloud = MOCK, type = {DELETE_INSTANCES, LAUNCH_INSTANCES, REBOOT_INSTANCES, HARD_REBOOT_INSTANCES, SNAPSHOT_INSTANCES})
public class UpdateInstancesOperation {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateInstancesOperation.class);
    
    @EntryPoint
    public void updateInstances(List<Instance> instances) {
        LOG.debug("Updating {} instances", instances.size());
    }
    
}
