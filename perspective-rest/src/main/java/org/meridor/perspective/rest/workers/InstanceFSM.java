package org.meridor.perspective.rest.workers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceStatus;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.*;
import org.meridor.perspective.rest.storage.Destination;
import org.meridor.perspective.rest.storage.Producer;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

import static org.meridor.perspective.beans.DestinationName.INSTANCES;

@Component
@FSM(start = InstanceQueuedEvent.class)
@Transitions({
        //Instance queue
        @Transit(from = InstanceQueuedEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        
        //Instance launch
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        
        //Instance soft reboot
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceRebootingEvent.class, to = InstanceRebootingEvent.class),
        @Transit(from = InstanceRebootingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceRebootingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        
        //Instance hard reboot
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceHardRebootingEvent.class, to = InstanceHardRebootingEvent.class),
        @Transit(from = InstanceHardRebootingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceHardRebootingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        
        //Instance shut off
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceShuttingDownEvent.class, to = InstanceShuttingDownEvent.class),
        @Transit(from = InstanceShuttingDownEvent.class, on = InstanceShutOffEvent.class, to = InstanceShutOffEvent.class),
        @Transit(from = InstanceShuttingDownEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceShutOffEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceShutOffEvent.class, on = InstanceDeletingEvent.class, stop = true),
        
        //Instance suspend
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceSuspendingEvent.class, to = InstanceSuspendingEvent.class),
        @Transit(from = InstanceSuspendingEvent.class, on = InstanceSuspendedEvent.class, to = InstanceSuspendedEvent.class),
        @Transit(from = InstanceSuspendingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceSuspendedEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceSuspendedEvent.class, on = InstanceDeletingEvent.class, stop = true),
        
        //Instance pause
        @Transit(from = InstanceLaunchedEvent.class, on = InstancePausingEvent.class, to = InstancePausingEvent.class),
        @Transit(from = InstancePausingEvent.class, on = InstancePausedEvent.class, to = InstancePausedEvent.class),
        @Transit(from = InstancePausingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstancePausedEvent.class, on = InstanceResumingEvent.class, to = InstanceResumingEvent.class),
        @Transit(from = InstanceResumingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstancePausedEvent.class, on = InstanceDeletingEvent.class, stop = true),
        
        //Instance snapshot
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceSnapshottingEvent.class, to = InstanceSnapshottingEvent.class),
        @Transit(from = InstanceSnapshottingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceSnapshottingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        
        //Instance rebuild
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceRebuildingEvent.class, to = InstanceRebuildingEvent.class),
        @Transit(from = InstanceRebuildingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceRebuildingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        
        //Instance resize
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceResizingEvent.class, to = InstanceResizingEvent.class),
        @Transit(from = InstanceResizingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceResizingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        
        //Instance migrate
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceMigratingEvent.class, to = InstanceMigratingEvent.class),
        @Transit(from = InstanceMigratingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceMigratingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        
        //Instance removal
        @Transit(from = InstanceErrorEvent.class, on = InstanceDeletingEvent.class, stop = true)
})
public class InstanceFSM {
    
    private static final Logger LOG = LoggerFactory.getLogger(InstanceFSM.class);

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private Storage storage;
    
    @Destination(INSTANCES)
    private Producer producer;
    
    @OnTransit
    public void onInstanceLaunching(InstanceLaunchingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Launching instance {}", instance);
        if (operationProcessor.supply(cloudType, OperationType.LAUNCH_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.LAUNCHING);
            storage.saveInstance(instance);
        } else {
            throw new InstanceException("Failed to launch", instance);
        }
    }

    @OnTransit
    public void onInstanceDeleting(InstanceDeletingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        if (storage.instanceExists(instance)) {
            LOG.info("Deleting instance {} in cloud {}", instance.getId(), cloudType);
            if (operationProcessor.supply(cloudType, OperationType.DELETE_INSTANCE, () -> instance)) {
                instance.setStatus(InstanceStatus.DELETING);
                storage.saveInstance(instance);
            } else {
                throw new InstanceException("Failed to delete", instance);
            }
        } else {
            LOG.error("Can't delete instance {} - not exists", instance.getId());
        }
    }
    
    @OnTransit
    public void onInstanceError(InstanceErrorEvent event) {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Changing cloud {} instance {} status to error", instance.getId(), cloudType);
        instance.setStatus(InstanceStatus.ERROR);
        instance.setErrorReason(event.getErrorReason());
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onUnknownEvent(InstanceEvent event) {
        LOG.warn("Discovered unknown event {}. Skipping it.", event);
    }
    
    @OnException
    public void onInstanceException(InstanceException e) {
        Instance instance = e.getInstance();
        instance.setStatus(InstanceStatus.ERROR);
        String errorReason = e.getMessage();
        instance.setErrorReason(errorReason);
        LOG.info("Setting instance {} status to ERROR with reason message: '{}'", instance, errorReason);
        storage.saveInstance(instance);
    }
    
    @OnException
    public void onUnsupportedOperationException(UnsupportedOperationException e){
        LOG.error("Trying to do an unsupported operation", e);
    }
    
    @OnException
    public void onException(Exception e){
        LOG.error("An uncaught exception discovered", e);
    }

}
