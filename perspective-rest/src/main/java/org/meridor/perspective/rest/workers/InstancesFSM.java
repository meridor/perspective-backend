package org.meridor.perspective.rest.workers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.*;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@FSM(start = InstancesNotLaunchedEvent.class)
@Transitions({
        //Instances queue
        @Transit(from = InstancesNotLaunchedEvent.class, on = InstancesQueuedEvent.class, to = InstancesQueuedEvent.class),
        @Transit(from = InstancesQueuedEvent.class, on = InstancesLaunchingEvent.class, to = InstancesLaunchingEvent.class),
        
        //Instances launch
        @Transit(from = InstancesLaunchingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        @Transit(from = InstancesLaunchingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        
        //Instances soft reboot
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesRebootingEvent.class, to = InstancesRebootingEvent.class),
        @Transit(from = InstancesRebootingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesRebootingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        
        //Instances hard reboot
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesHardRebootingEvent.class, to = InstancesHardRebootingEvent.class),
        @Transit(from = InstancesHardRebootingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesHardRebootingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        
        //Instances shut off
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesShuttingDownEvent.class, to = InstancesShuttingDownEvent.class),
        @Transit(from = InstancesShuttingDownEvent.class, on = InstancesShutOffEvent.class, to = InstancesShutOffEvent.class),
        @Transit(from = InstancesShuttingDownEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesShutOffEvent.class, on = InstancesLaunchingEvent.class, to = InstancesLaunchingEvent.class),
        @Transit(from = InstancesShutOffEvent.class, on = InstancesDeletingEvent.class, to = InstancesDeletingEvent.class),
        
        //Instances suspend
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesSuspendingEvent.class, to = InstancesSuspendingEvent.class),
        @Transit(from = InstancesSuspendingEvent.class, on = InstancesSuspendedEvent.class, to = InstancesSuspendedEvent.class),
        @Transit(from = InstancesSuspendingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesSuspendedEvent.class, on = InstancesLaunchingEvent.class, to = InstancesLaunchingEvent.class),
        @Transit(from = InstancesSuspendedEvent.class, on = InstancesDeletingEvent.class, to = InstancesDeletingEvent.class),
        
        //Instances pause
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesPausingEvent.class, to = InstancesPausingEvent.class),
        @Transit(from = InstancesPausingEvent.class, on = InstancesPausedEvent.class, to = InstancesPausedEvent.class),
        @Transit(from = InstancesPausingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesPausedEvent.class, on = InstancesResumingEvent.class, to = InstancesResumingEvent.class),
        @Transit(from = InstancesResumingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        @Transit(from = InstancesPausedEvent.class, on = InstancesDeletingEvent.class, to = InstancesDeletingEvent.class),
        
        //Instances snapshot
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesSnapshottingEvent.class, to = InstancesSnapshottingEvent.class),
        @Transit(from = InstancesSnapshottingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesSnapshottingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        
        //Instances rebuild
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesRebuildingEvent.class, to = InstancesRebuildingEvent.class),
        @Transit(from = InstancesRebuildingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesRebuildingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        
        //Instances resize
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesResizingEvent.class, to = InstancesResizingEvent.class),
        @Transit(from = InstancesResizingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesResizingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        
        //Instances migrate
        @Transit(from = InstancesLaunchedEvent.class, on = InstancesMigratingEvent.class, to = InstancesMigratingEvent.class),
        @Transit(from = InstancesMigratingEvent.class, on = InstancesErrorEvent.class, to = InstancesErrorEvent.class),
        @Transit(from = InstancesMigratingEvent.class, on = InstancesLaunchedEvent.class, to = InstancesLaunchedEvent.class),
        
        //Instances removal
        @Transit(from = InstancesErrorEvent.class, on = InstancesDeletingEvent.class, to = InstancesDeletingEvent.class),
        @Transit(from = InstancesDeletingEvent.class, on = InstancesNotLaunchedEvent.class, to = InstancesNotLaunchedEvent.class, stop = true),
})
public class InstancesFSM {
    
    private static final Logger LOG = LoggerFactory.getLogger(InstancesFSM.class);

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private Storage storage;

    @OnTransit
    public void onInstancesQueued(InstancesQueuedEvent event) {
        List<Instance> instances = event.getInstances();
        LOG.debug("Queued {} instances for launch", instances.size());
        //TODO: to be implemented!
    }

    @OnTransit
    public void onInstancesDeleting(InstancesDeletingEvent event) {
        CloudType cloudType = event.getCloudType();
        List<Instance> instances = event.getInstances();
        String instancesUuids = instances.stream().map(Instance::getId).collect(Collectors.joining(", "));
        try {
            LOG.info("Deleting instances {} in cloud {}", instancesUuids, cloudType);
            if (!operationProcessor.supply(cloudType, OperationType.DELETE_INSTANCES, () -> instances)) {
                throw new RuntimeException("Failed to delete instances from the cloud");
            }
            storage.deleteInstances(cloudType, instances);
        } catch (Exception e) {
            LOG.error("Failed to delete instances in cloud " + cloudType, e);
        }
    }

    @OnException
    public void onException(Exception e){
        LOG.error("An uncaught exception discovered", e);
    }

}
