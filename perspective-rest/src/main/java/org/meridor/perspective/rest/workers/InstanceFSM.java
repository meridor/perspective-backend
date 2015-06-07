package org.meridor.perspective.rest.workers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceStatus;
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

import static org.meridor.perspective.events.EventFactory.instanceEvent;

@Component
@FSM(start = InstanceNotAvailableEvent.class)
@Transitions({
        //Instance sync
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceQueuedEvent.class, to = InstanceQueuedEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceRebootingEvent.class, to = InstanceRebootingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceHardRebootingEvent.class, to = InstanceHardRebootingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceShuttingDownEvent.class, to = InstanceShuttingDownEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceShutOffEvent.class, to = InstanceShutOffEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstancePausingEvent.class, to = InstancePausingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstancePausedEvent.class, to = InstancePausedEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceResumingEvent.class, to = InstanceResumingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceSnapshottingEvent.class, to = InstanceSnapshottingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceRebuildingEvent.class, to = InstanceRebuildingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceResizingEvent.class, to = InstanceResizingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceMigratingEvent.class, to = InstanceMigratingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceDeletingEvent.class, to = InstanceDeletingEvent.class),
        
        //Instance launch
        @Transit(from = InstanceQueuedEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
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
    
    @BeforeTransit
    public void beforeTransit(InstanceEvent instanceEvent) {
        LOG.trace("Doing transition for event {}", instanceEvent);
    }
    
    @OnTransit
    public void onInstanceQueued(InstanceQueuedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as queued", instance.getCloudType(), instance.getId());
            instance.setStatus(InstanceStatus.QUEUED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceLaunching(InstanceLaunchingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Launching cloud {} instance {}", instance.getCloudType(), instance);
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.LAUNCH_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.LAUNCHING);
            storage.saveInstance(instance);
        } else {
            throw new InstanceException("Failed to launch", instance);
        }
    }
    @OnTransit
    public void onInstanceLaunched(InstanceLaunchedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as launched", instance.getCloudType(), instance.getId());
            instance.setStatus(InstanceStatus.LAUNCHED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceRebooting(InstanceRebootingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Rebooting cloud {} instance {}", instance.getCloudType(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.REBOOT_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.REBOOTING);
        } else {
            instance.setErrorReason("Failed to reboot");
        }
        storage.saveInstance(instance);
    }
    
    @OnTransit
    public void onInstanceHardRebooting(InstanceHardRebootingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Hard rebooting cloud {} instance {}", instance.getCloudType(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.HARD_REBOOT_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.HARD_REBOOTING);
        } else {
            instance.setErrorReason("Failed to hard reboot");
        }
        storage.saveInstance(instance);
    }
    
    @OnTransit
    public void onInstanceShuttingDown(InstanceShuttingDownEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Shutting down cloud {} instance {}", instance.getCloudType(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.SHUTDOWN_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.SHUTTING_DOWN);
        } else {
            instance.setErrorReason("Failed to shut down");
        }
        storage.saveInstance(instance);
    }
    
    @OnTransit
    public void onInstanceShutoff(InstanceShutOffEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as shutoff", instance.getCloudType(), instance.getId());
            instance.setStatus(InstanceStatus.SHUTOFF);
            storage.saveInstance(instance);
        }
    }
    @OnTransit
    public void onInstancePausing(InstancePausingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Pausing cloud {} instance {}", cloudType, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.PAUSE_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.PAUSING);
        } else {
            instance.setErrorReason("Failed to pause");
        }
        storage.saveInstance(instance);
    }
    
    @OnTransit
    public void onInstancePaused(InstancePausedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as paused", instance.getCloudType(), instance.getId());
            instance.setStatus(InstanceStatus.PAUSED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceResuming(InstanceResumingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Resuming cloud {} instance {}", cloudType, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.RESUME_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.RESUMING);
        } else {
            instance.setErrorReason("Failed to resume");
        }
        storage.saveInstance(instance);
    }
    
    @OnTransit
    public void onInstanceRebuilding(InstanceRebuildingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Rebuilding cloud {} instance {}", cloudType, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.REBUILD_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.REBUILDING);
        } else {
            instance.setErrorReason("Failed to rebuild");
        }
        storage.saveInstance(instance);
    }
    
    @OnTransit
    public void onInstanceResizing(InstanceResizingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Resizing cloud {} instance {}", cloudType, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.RESIZE_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.RESIZING);
        } else {
            instance.setErrorReason("Failed to resize");
        }
        storage.saveInstance(instance);
    }
    
    @OnTransit
    public void onInstanceSuspending(InstanceSuspendingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Suspending cloud {} instance {}", instance.getCloudType(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.SUSPEND_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.SUSPENDING);
        } else {
            instance.setErrorReason("Failed to suspend");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceSuspended(InstanceSuspendedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.debug("Marking cloud {} instance {} as suspended", instance.getCloudType(), instance.getId());
            instance.setStatus(InstanceStatus.SUSPENDED);
            storage.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceMigrating(InstanceMigratingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Migrating cloud {} instance {}", cloudType, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloudType, OperationType.MIGRATE_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.MIGRATING);
        } else {
            instance.setErrorReason("Failed to migrate");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceDeleting(InstanceDeletingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        if (storage.instanceExists(instance)) {
            LOG.info("Deleting cloud {} instance {}", cloudType, instance.getId());
            if (event.isSync() || operationProcessor.supply(cloudType, OperationType.DELETE_INSTANCE, () -> instance)) {
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
    public void onInstanceSnapshotting(InstanceSnapshottingEvent event) throws Exception {
        Instance instance = event.getInstance();
        CloudType cloudType = instance.getCloudType();
        LOG.info("Taking cloud {} instance {} snapshot", cloudType, instance.getId());
        if (operationProcessor.supply(cloudType, OperationType.SNAPSHOT_INSTANCE, () -> instance)) {
            instance.setStatus(InstanceStatus.SNAPSHOTTING);
        } else {
            instance.setErrorReason("Failed to take snapshot");
        }
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceError(InstanceErrorEvent event) {
        Instance instance = event.getInstance();
        LOG.info("Changing cloud {} instance {} status to error", instance.getCloudType(), instance.getId());
        instance.setStatus(InstanceStatus.ERROR);
        instance.setErrorReason(event.getErrorReason());
        storage.saveInstance(instance);
    }

    @OnTransit
    public void onUnknownEvent(InstanceEvent event) {
        LOG.warn("Skipping unknown event {}", event);
    }
    
    @OnException
    public void onInstanceException(InstanceException e) throws Exception {
        InstanceErrorEvent event = instanceEvent(InstanceErrorEvent.class, e.getInstance());
        event.setErrorReason(e.getMessage());
        onInstanceError(event);
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
