package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

import java.util.Optional;

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
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceStartingEvent.class, to = InstanceStartingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceSuspendingEvent.class, to = InstanceSuspendingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceSuspendedEvent.class, to = InstanceSuspendedEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceRebuildingEvent.class, to = InstanceRebuildingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceResizingEvent.class, to = InstanceResizingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceMigratingEvent.class, to = InstanceMigratingEvent.class),
        @Transit(from = InstanceNotAvailableEvent.class, on = InstanceDeletingEvent.class, to = InstanceDeletingEvent.class),
        @Transit(from = InstanceQueuedEvent.class, on = InstanceQueuedEvent.class, to = InstanceQueuedEvent.class),
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstanceRebootingEvent.class, on = InstanceRebootingEvent.class, to = InstanceRebootingEvent.class),
        @Transit(from = InstanceHardRebootingEvent.class, on = InstanceHardRebootingEvent.class, to = InstanceHardRebootingEvent.class),
        @Transit(from = InstanceShuttingDownEvent.class, on = InstanceShuttingDownEvent.class, to = InstanceShuttingDownEvent.class),
        @Transit(from = InstanceShutOffEvent.class, on = InstanceShutOffEvent.class, to = InstanceShutOffEvent.class),
        @Transit(from = InstanceErrorEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstancePausingEvent.class, on = InstancePausingEvent.class, to = InstancePausingEvent.class),
        @Transit(from = InstancePausedEvent.class, on = InstancePausedEvent.class, to = InstancePausedEvent.class),
        @Transit(from = InstanceResumingEvent.class, on = InstanceResumingEvent.class, to = InstanceResumingEvent.class),
        @Transit(from = InstanceStartingEvent.class, on = InstanceStartingEvent.class, to = InstanceStartingEvent.class),
        @Transit(from = InstanceSuspendingEvent.class, on = InstanceSuspendingEvent.class, to = InstanceSuspendingEvent.class),
        @Transit(from = InstanceSuspendedEvent.class, on = InstanceSuspendedEvent.class, to = InstanceSuspendedEvent.class),
        @Transit(from = InstanceSnapshottingEvent.class, on = InstanceSnapshottingEvent.class, to = InstanceSnapshottingEvent.class),
        @Transit(from = InstanceRebuildingEvent.class, on = InstanceRebuildingEvent.class, to = InstanceRebuildingEvent.class),
        @Transit(from = InstanceResizingEvent.class, on = InstanceResizingEvent.class, to = InstanceResizingEvent.class),
        @Transit(from = InstanceMigratingEvent.class, on = InstanceMigratingEvent.class, to = InstanceMigratingEvent.class),
        @Transit(from = InstanceDeletingEvent.class, on = InstanceDeletingEvent.class, to = InstanceDeletingEvent.class),

        //Instance launch
        @Transit(from = InstanceQueuedEvent.class, on = InstanceLaunchingEvent.class, to = InstanceLaunchingEvent.class),
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceLaunchedEvent.class, to = InstanceLaunchedEvent.class),
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceShutOffEvent.class, to = InstanceShutOffEvent.class), //e.g. for Docker
        @Transit(from = InstanceLaunchingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceDeletingEvent.class, stop = true),

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
        @Transit(from = InstanceShutOffEvent.class, on = InstanceStartingEvent.class, to = InstanceStartingEvent.class),
        @Transit(from = InstanceShutOffEvent.class, on = InstanceDeletingEvent.class, stop = true),

        //Instance suspend
        @Transit(from = InstanceLaunchedEvent.class, on = InstanceSuspendingEvent.class, to = InstanceSuspendingEvent.class),
        @Transit(from = InstanceSuspendingEvent.class, on = InstanceSuspendedEvent.class, to = InstanceSuspendedEvent.class),
        @Transit(from = InstanceSuspendingEvent.class, on = InstanceErrorEvent.class, to = InstanceErrorEvent.class),
        @Transit(from = InstanceSuspendedEvent.class, on = InstanceResumingEvent.class, to = InstanceResumingEvent.class),
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

    private final OperationProcessor operationProcessor;

    private final CloudConfigurationProvider cloudConfigurationProvider;

    private final InstancesAware instancesAware;

    @Autowired
    public InstanceFSM(OperationProcessor operationProcessor, InstancesAware instancesAware, CloudConfigurationProvider cloudConfigurationProvider) {
        this.operationProcessor = operationProcessor;
        this.cloudConfigurationProvider = cloudConfigurationProvider;
        this.instancesAware = instancesAware;
    }

    @BeforeTransit
    public void beforeTransit(InstanceEvent instanceEvent) {
        LOG.trace("Doing transition for event {}", instanceEvent);
    }

    @OnTransit
    public void onInstanceQueued(InstanceQueuedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as queued", instance.getName(), instance.getId());
            instance.setState(InstanceState.QUEUED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceLaunching(InstanceLaunchingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (event.isSync()) {
            LOG.info("Marking instance {} ({}) as launching", instance.getName(), instance.getId());
            instance.setState(InstanceState.LAUNCHING);
            instancesAware.saveInstance(instance);
        } else {
            LOG.info("Adding instance {} ({})", instance.getName(), instance.getId());
            Optional<Instance> updatedInstanceCandidate = operationProcessor.process(cloud, OperationType.ADD_INSTANCE, () -> instance);
            if (!updatedInstanceCandidate.isPresent()) {
                throw new RuntimeException(String.format("Failed to add %s", instance));
            }
            Instance updatedInstance = updatedInstanceCandidate.get();
            updatedInstance.setState(InstanceState.LAUNCHING);
            /* 
                Initially we assign some random UUID to instance while it's being created (just to show that instance was queued).
                When real ID becomes available we replace initially queued instance by one with correct ID. 
             */

            String temporaryInstanceId = event.getTemporaryInstanceId();
            if (temporaryInstanceId != null && instancesAware.instanceExists(temporaryInstanceId)) {
                instancesAware.deleteInstance(temporaryInstanceId);
            }
            instancesAware.saveInstance(updatedInstance);
        }
    }

    @OnTransit
    public void onInstanceLaunched(InstanceLaunchedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as launched", instance.getName(), instance.getId());
            instance.setState(InstanceState.LAUNCHED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceRebooting(InstanceRebootingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Rebooting instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.REBOOT_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.REBOOTING);
        } else {
            throw new RuntimeException(String.format("Failed to reboot %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceHardRebooting(InstanceHardRebootingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Hard rebooting cloud {} instance {}", cloudId, instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.HARD_REBOOT_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.HARD_REBOOTING);
        } else {
            throw new RuntimeException(String.format("Failed to hard reboot %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceShuttingDown(InstanceShuttingDownEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Shutting down instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.SHUTDOWN_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.SHUTTING_DOWN);
        } else {
            throw new RuntimeException(String.format("Failed to shut down %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceShutoff(InstanceShutOffEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as shutoff", instance.getName(), instance.getId());
            instance.setState(InstanceState.SHUTOFF);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstancePausing(InstancePausingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Pausing instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.PAUSE_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.PAUSING);
        } else {
            throw new RuntimeException(String.format("Failed to pause %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstancePaused(InstancePausedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as paused", instance.getName(), instance.getId());
            instance.setState(InstanceState.PAUSED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceResuming(InstanceResumingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Resuming instance {} ({})", instance.getName(), instance.getId());
        OperationType operationType = event.getOperationType() != null ?
                event.getOperationType() : OperationType.RESUME_INSTANCE;
        if (event.isSync() || operationProcessor.supply(cloud, operationType, () -> instance)) {
            instance.setState(InstanceState.RESUMING);
        } else {
            throw new RuntimeException(String.format("Failed to resume %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceRebuilding(InstanceRebuildingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Rebuilding instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.REBUILD_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.REBUILDING);
        } else {
            throw new RuntimeException(String.format("Failed to rebuild %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceResizing(InstanceResizingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Resizing instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.RESIZE_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.RESIZING);
        } else {
            throw new RuntimeException(String.format("Failed to resize %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceStarting(InstanceStartingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Starting instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.START_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.STARTING);
        } else {
            throw new RuntimeException(String.format("Failed to start %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceSuspending(InstanceSuspendingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Suspending instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.SUSPEND_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.SUSPENDING);
        } else {
            throw new RuntimeException(String.format("Failed to suspend %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceSuspended(InstanceSuspendedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as suspended", instance.getName(), instance.getId());
            instance.setState(InstanceState.SUSPENDED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceMigrating(InstanceMigratingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        LOG.info("Migrating instance {} ({})", instance.getName(), instance.getId());
        if (event.isSync() || operationProcessor.supply(cloud, OperationType.MIGRATE_INSTANCE, () -> instance)) {
            instance.setState(InstanceState.MIGRATING);
        } else {
            throw new RuntimeException(String.format("Failed to migrate %s", instance));
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceDeleting(InstanceDeletingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (event.isSync()) {
            LOG.info("Marking instance {} ({}) as deleting", instance.getName(), instance.getId());
            instance.setState(InstanceState.DELETING);
            instancesAware.saveInstance(instance);
        } else if (instancesAware.instanceExists(instance.getId())) {
            if (operationProcessor.supply(cloud, OperationType.DELETE_INSTANCE, () -> instance)) {
                LOG.info("Deleting instance {} ({})", instance.getName(), instance.getId());
                instancesAware.deleteInstance(instance.getId());
            } else {
                throw new RuntimeException(String.format("Failed to delete %s", instance));
            }
        } else {
            LOG.error("Can't delete instance {} ({}) - not exists", instance.getName(), instance.getId());
        }
    }

    @OnTransit
    public void onInstanceSnapshotting(InstanceSnapshottingEvent event) {
        Instance instance = event.getInstance();
        if (event.isSync()) {
            LOG.info("Marking instance {} ({}) as snapshotting", instance.getName(), instance.getId());
            instance.setState(InstanceState.SNAPSHOTTING);
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceError(InstanceErrorEvent event) {
        Instance instance = event.getInstance();
        LOG.info("Changing instance {} ({}) status to error with reason = {}", instance.getName(), instance.getId(), event.getErrorReason());
        instance.setState(InstanceState.ERROR);
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onUnknownEvent(InstanceEvent event) {
        LOG.warn("Skipping unknown event {}", event);
    }

    @OnException
    public void onUnsupportedOperationException(UnsupportedOperationException e) {
        LOG.error("Trying to do an unsupported operation", e);
    }

}
