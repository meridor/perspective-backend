package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.meridor.perspective.worker.processor.event.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

import java.util.Optional;

import static org.meridor.perspective.events.EventFactory.instanceEventToState;
import static org.meridor.perspective.worker.processor.event.EventUtils.requestProjectSync;

@Component
@FSM(start = InstanceNotAvailableEvent.class)
@Transitions({
        //Instance sync
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceQueuedEvent.class},
                on = InstanceQueuedEvent.class,
                to = InstanceQueuedEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceQueuedEvent.class, InstanceLaunchingEvent.class},
                on = InstanceLaunchingEvent.class,
                to = InstanceLaunchingEvent.class
        ),
        @Transit(
                from = {
                        InstanceNotAvailableEvent.class,
                        InstanceLaunchingEvent.class,
                        InstanceLaunchedEvent.class,
                        InstanceRebootingEvent.class,
                        InstanceHardRebootingEvent.class,
                        InstanceStartingEvent.class,
                        InstanceResumingEvent.class,
                        InstanceSnapshottingEvent.class,
                        InstanceRebuildingEvent.class,
                        InstanceResizingEvent.class,
                        InstanceMigratingEvent.class
                },
                on = InstanceLaunchedEvent.class,
                to = InstanceLaunchedEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceRebootingEvent.class},
                on = InstanceRebootingEvent.class,
                to = InstanceRebootingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceHardRebootingEvent.class},
                on = InstanceHardRebootingEvent.class,
                to = InstanceHardRebootingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceShuttingDownEvent.class},
                on = InstanceShuttingDownEvent.class,
                to = InstanceShuttingDownEvent.class
        ),
        @Transit(
                from = {
                        InstanceNotAvailableEvent.class,
                        InstanceLaunchingEvent.class, //E.g. for Docker
                        InstanceLaunchedEvent.class,
                        InstanceShuttingDownEvent.class,
                        InstanceShutOffEvent.class
                },
                on = InstanceShutOffEvent.class,
                to = InstanceShutOffEvent.class
        ),
        @Transit(
                from = {
                        InstanceNotAvailableEvent.class,
                        InstanceLaunchingEvent.class,
                        InstanceRebootingEvent.class,
                        InstanceHardRebootingEvent.class,
                        InstanceShuttingDownEvent.class,
                        InstanceStartingEvent.class,
                        InstancePausingEvent.class,
                        InstanceRebuildingEvent.class,
                        InstanceResizingEvent.class,
                        InstanceSnapshottingEvent.class,
                        InstanceSuspendingEvent.class,
                        InstanceMigratingEvent.class,
                        InstanceErrorEvent.class
                },
                on = InstanceErrorEvent.class,
                to = InstanceErrorEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstancePausingEvent.class},
                on = InstancePausingEvent.class,
                to = InstancePausingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstancePausingEvent.class, InstancePausedEvent.class},
                on = InstancePausedEvent.class,
                to = InstancePausedEvent.class
        ),
        @Transit(
                from = {
                        InstanceNotAvailableEvent.class,
                        InstancePausedEvent.class,
                        InstanceSuspendedEvent.class,
                        InstanceResumingEvent.class
                },
                on = InstanceResumingEvent.class,
                to = InstanceResumingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceSnapshottingEvent.class},
                on = InstanceSnapshottingEvent.class,
                to = InstanceSnapshottingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceStartingEvent.class, InstanceShutOffEvent.class},
                on = InstanceStartingEvent.class,
                to = InstanceStartingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceSuspendingEvent.class},
                on = InstanceSuspendingEvent.class,
                to = InstanceSuspendingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceSuspendingEvent.class, InstanceSuspendedEvent.class},
                on = InstanceSuspendedEvent.class,
                to = InstanceSuspendedEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceRebuildingEvent.class},
                on = InstanceRebuildingEvent.class,
                to = InstanceRebuildingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceResizingEvent.class},
                on = InstanceResizingEvent.class,
                to = InstanceResizingEvent.class
        ),
        @Transit(
                from = {InstanceNotAvailableEvent.class, InstanceLaunchedEvent.class, InstanceMigratingEvent.class},
                on = InstanceMigratingEvent.class,
                to = InstanceMigratingEvent.class
        ),
        @Transit(
                from = {
                        InstanceNotAvailableEvent.class,
                        InstanceLaunchedEvent.class,
                        InstanceShutOffEvent.class,
                        InstanceSuspendedEvent.class,
                        InstancePausedEvent.class,
                        InstanceErrorEvent.class,
                        InstanceDeletingEvent.class
                },
                on = InstanceDeletingEvent.class,
                to = InstanceDeletingEvent.class
        ),
        @Transit(on = InstanceRenamingEvent.class)
        
})
public class InstanceFSM {

    private static final Logger LOG = LoggerFactory.getLogger(InstanceFSM.class);

    private final OperationProcessor operationProcessor;

    private final CloudConfigurationProvider cloudConfigurationProvider;

    private final InstancesAware instancesAware;

    private final EventBus eventBus;

    private final MailSender mailSender;

    @Autowired
    public InstanceFSM(OperationProcessor operationProcessor, InstancesAware instancesAware, CloudConfigurationProvider cloudConfigurationProvider, EventBus eventBus, MailSender mailSender) {
        this.operationProcessor = operationProcessor;
        this.cloudConfigurationProvider = cloudConfigurationProvider;
        this.instancesAware = instancesAware;
        this.eventBus = eventBus;
        this.mailSender = mailSender;
    }

    @BeforeTransit
    public void beforeTransit(@Event InstanceEvent instanceEvent) {
        LOG.trace("Doing transition for event {}", instanceEvent);
    }

    @OnTransit
    public void onInstanceQueued(@Event InstanceQueuedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as queued", instance.getName(), instance.getId());
            instance.setState(InstanceState.QUEUED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceLaunching(@Event InstanceLaunchingEvent event) {
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
            requestProjectSync(eventBus, cloud, updatedInstance.getProjectId());
        }
    }

    @OnTransit
    public void onInstanceLaunched(@Event InstanceLaunchedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as launched", instance.getName(), instance.getId());
            instance.setState(InstanceState.LAUNCHED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceRebooting(@Event InstanceRebootingEvent event) {
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
    public void onInstanceHardRebooting(@Event InstanceHardRebootingEvent event) {
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
    public void onInstanceShuttingDown(@Event InstanceShuttingDownEvent event) {
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
    public void onInstanceShutoff(@Event InstanceShutOffEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as shutoff", instance.getName(), instance.getId());
            instance.setState(InstanceState.SHUTOFF);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstancePausing(@Event InstancePausingEvent event) {
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
    public void onInstancePaused(@Event InstancePausedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as paused", instance.getName(), instance.getId());
            instance.setState(InstanceState.PAUSED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceResuming(@Event InstanceResumingEvent event) {
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
    public void onInstanceRebuilding(@Event InstanceRebuildingEvent event) {
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
    public void onInstanceResizing(@Event InstanceResizingEvent event) {
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
        requestProjectSync(eventBus, cloud, instance.getProjectId());
    }

    @OnTransit
    public void onInstanceStarting(@Event InstanceStartingEvent event) {
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
    public void onInstanceSuspending(@Event InstanceSuspendingEvent event) {
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
    public void onInstanceSuspended(@Event InstanceSuspendedEvent event) {
        if (event.isSync()) {
            Instance instance = event.getInstance();
            LOG.info("Marking instance {} ({}) as suspended", instance.getName(), instance.getId());
            instance.setState(InstanceState.SUSPENDED);
            instancesAware.saveInstance(instance);
        }
    }

    @OnTransit
    public void onInstanceMigrating(@Event InstanceMigratingEvent event) {
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
    public void onInstanceDeleting(@Event InstanceDeletingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (event.isSync()) {
            LOG.info("Marking instance {} ({}) as deleting", instance.getName(), instance.getId());
            instance.setState(InstanceState.DELETING);
            instancesAware.saveInstance(instance);
        } else if (instancesAware.instanceExists(instance.getId())) {
            LOG.info("Deleting instance {} ({})", instance.getName(), instance.getId());
            if (operationProcessor.supply(cloud, OperationType.DELETE_INSTANCE, () -> instance)) {
                instancesAware.deleteInstance(instance.getId());
                requestProjectSync(eventBus, cloud, instance.getProjectId());
            } else {
                throw new RuntimeException(String.format("Failed to delete %s", instance));
            }
        } else {
            LOG.error("Can't delete instance {} ({}) - not exists", instance.getName(), instance.getId());
        }
    }

    @OnTransit
    public void onInstanceRenaming(@Event InstanceRenamingEvent event) {
        Instance instance = event.getInstance();
        String cloudId = instance.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        Optional<Instance> instanceCandidate = instancesAware.getInstance(instance.getId());
        instanceCandidate.ifPresent(i -> {
            LOG.info("Renaming instance {} ({}) to {}", i.getName(), i.getId(), instance.getName());
            if (operationProcessor.supply(cloud, OperationType.RENAME_INSTANCE, () -> instance)) {
                instancesAware.saveInstance(instance);
            } else {
                throw new RuntimeException(String.format(
                        "Can't rename instance %s (%s) to %s",
                        i.getName(),
                        i.getId(),
                        instance.getName()
                ));
            }
        });
    }

    @OnTransit
    public void onInstanceSnapshotting(@Event InstanceSnapshottingEvent event) {
        Instance instance = event.getInstance();
        if (event.isSync()) {
            LOG.info("Marking instance {} ({}) as snapshotting", instance.getName(), instance.getId());
            instance.setState(InstanceState.SNAPSHOTTING);
        }
        instancesAware.saveInstance(instance);
    }

    @OnTransit
    public void onInstanceError(@FromState InstanceEvent from, @Event InstanceErrorEvent event) {
        Instance instance = event.getInstance();
        LOG.info("Changing instance {} ({}) status to error", instance.getName(), instance.getId());
        instance.setState(InstanceState.ERROR);
        instancesAware.saveInstance(instance);
        if (!(from instanceof InstanceErrorEvent) && !(from instanceof InstanceNotAvailableEvent)) {
            LOG.info("Sending letter about instance {} ({}) error", instance.getName(), instance.getId());
            String message = getMailMessage(instanceEventToState(from), instance);
            mailSender.sendLetter(message);
        }
    }

    private String getMailMessage(InstanceState previousInstanceState, Instance instance) {
        switch (previousInstanceState) {
            case DELETING:
                return String.format("Failed to delete instance %s (%s)", instance.getName(), instance.getId());
            case HARD_REBOOTING:
                return String.format("Failed to hard reboot instance %s (%s)", instance.getName(), instance.getId());
            case LAUNCHED:
            case PAUSED:
            case SHUTOFF:
            case SUSPENDED:
                return String.format("Instance %s (%s) crashed", instance.getName(), instance.getId());
            case LAUNCHING:
            case QUEUED:
                return String.format("Failed to launch instance %s (%s)", instance.getName(), instance.getId());
            case MIGRATING:
                return String.format("Failed to migrate instance %s (%s)", instance.getName(), instance.getId());
            case PAUSING:
                return String.format("Failed to pause instance %s (%s)", instance.getName(), instance.getId());
            case REBOOTING:
                return String.format("Failed to reboot instance %s (%s)", instance.getName(), instance.getId());
            case REBUILDING:
                return String.format("Failed to rebuild instance %s (%s)", instance.getName(), instance.getId());
            case RESIZING:
                return String.format("Failed to resize instance %s (%s)", instance.getName(), instance.getId());
            case RESUMING:
                return String.format("Failed to resume instance %s (%s)", instance.getName(), instance.getId());
            case SHUTTING_DOWN:
                return String.format("Failed to shutdown instance %s (%s)", instance.getName(), instance.getId());
            case STARTING:
                return String.format("Failed to start instance %s (%s)", instance.getName(), instance.getId());
            case SUSPENDING:
                return String.format("Failed to suspend instance %s (%s)", instance.getName(), instance.getId());
        }
        throw new IllegalArgumentException(String.format(
                "Unsupported instance state: %s. This is a bug.",
                previousInstanceState.value()
        ));
    }

    @OnException
    public void onUnsupportedOperationException(UnsupportedOperationException e) {
        LOG.error("Trying to do an unsupported operation", e);
    }

}
