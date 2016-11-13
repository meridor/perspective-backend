package org.meridor.perspective.rest.services;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.backend.messaging.Destination;
import org.meridor.perspective.backend.messaging.Producer;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.backend.storage.OperationsRegistry;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.meridor.perspective.beans.DestinationName.WRITE_TASKS;
import static org.meridor.perspective.beans.InstanceState.*;
import static org.meridor.perspective.config.OperationType.*;
import static org.meridor.perspective.events.EventFactory.*;
import static org.meridor.perspective.backend.messaging.MessageUtils.message;

@Service
public class InstancesService {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesService.class);

    private final InstancesAware instancesAware;

    private final ImagesAware imagesAware;

    private final ProjectsAware projectsAware;

    private final OperationsRegistry operationsRegistry;

    @Destination(WRITE_TASKS)
    private Producer producer;

    @Value("${perspective.messaging.max.retries:5}")
    private int maxRetries;

    @Autowired
    public InstancesService(
            InstancesAware instancesAware,
            ImagesAware imagesAware,
            ProjectsAware projectsAware,
            OperationsRegistry operationsRegistry
    ) {
        this.instancesAware = instancesAware;
        this.imagesAware = imagesAware;
        this.projectsAware = projectsAware;
        this.operationsRegistry = operationsRegistry;
    }

    public Optional<Instance> getInstanceById(String instanceId) {
        LOG.info("Getting instance with id = {}", instanceId);
        return instancesAware.getInstance(instanceId);
    }
    
    public void launchInstances(List<Instance> instances) {
        for (final Instance instance : instances) {
            LOG.info("Queuing instance {} for launch", instance);
            String temporaryId = uuid();
            instance.setId(temporaryId);
            instance.setCreated(now());
            instance.setTimestamp(now());
            instance.setState(InstanceState.QUEUED);
            if (instance.getCloudType() == null) {
                Optional<Project> projectCandidate = projectsAware.getProject(instance.getProjectId());
                if (projectCandidate.isPresent()) {
                    Project project = projectCandidate.get();
                    instance.setCloudType(project.getCloudType());
                    instance.setCloudId(project.getCloudId());
                } else {
                    LOG.error("Project {} not found while trying to determine new instance \"{}\" cloud type", instance.getProjectId(), instance.getName());
                }
            }
            instancesAware.saveInstance(instance);
            InstanceLaunchingEvent event = instanceEvent(InstanceLaunchingEvent.class, instance);
            event.setTemporaryInstanceId(temporaryId);
            producer.produce(message(instance.getCloudType(), event, maxRetries));
        }
    }

    public void startInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                ADD_INSTANCE,
                i -> {
                    i.setState(STARTING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for start", instanceId);
                    return instanceEvent(InstanceStartingEvent.class, i);
                }
        ));
    }

    public void shutdownInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                SHUTDOWN_INSTANCE,
                i -> {
                    i.setState(SHUTTING_DOWN);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for shutdown", instanceId);
                    return instanceEvent(InstanceShuttingDownEvent.class, i);
                }
        ));
    }

    public void rebootInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                REBOOT_INSTANCE,
                i -> {
                    i.setState(REBOOTING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for reboot", instanceId);
                    return instanceEvent(InstanceRebootingEvent.class, i);
                }
        ));
    }
    
    public void hardRebootInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                HARD_REBOOT_INSTANCE,
                i -> {
                    i.setState(HARD_REBOOTING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for hard reboot", instanceId);
                    return instanceEvent(InstanceHardRebootingEvent.class, i);
                }
        ));
    }

    public boolean rebuildInstances(String imageId, List<String> instanceIds) {
        return ifImageExists(imageId, image -> instanceIds.forEach(
                instanceId -> whenInstanceExists(
                        instanceId,
                        REBOOT_INSTANCE,
                        i -> {
                            i.setImage(image);
                            i.setState(REBUILDING);
                            return i;
                        },
                        i -> {
                            LOG.info("Queuing instance {} for rebuild to image {}", instanceId, imageId);
                            return instanceEvent(InstanceRebuildingEvent.class, i);
                        }
                )
        ));
    }

    public boolean resizeInstances(String flavorId, List<String> instanceIds) {
        return ifFlavorExists(flavorId, flavor -> instanceIds.forEach(
                instanceId -> whenInstanceExists(
                        instanceId,
                        RESIZE_INSTANCE,
                        i -> {
                            i.setFlavor(flavor);
                            i.setState(RESIZING);
                            return i;
                        },
                        i -> {
                            LOG.info("Queuing instance {} for resize to flavor {}", instanceId, flavorId);
                            return instanceEvent(InstanceResizingEvent.class, i);
                        }
                )
        ));
    }

    public void pauseInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                PAUSE_INSTANCE,
                i -> {
                    i.setState(PAUSING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for pause", instanceId);
                    return instanceEvent(InstancePausingEvent.class, i);
                }
        ));
    }

    public void resumeInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                RESUME_INSTANCE,
                i -> {
                    i.setState(RESUMING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for resume", instanceId);
                    //The main idea is to use the same resume command to restore suspended, paused or shelved instance
                    InstanceResumingEvent instanceResumingEvent = instanceEvent(InstanceResumingEvent.class, i);
                    instanceResumingEvent.setOperationType(
                            i.getState() == PAUSED ? UNPAUSE_INSTANCE : RESUME_INSTANCE
                    );
                    return instanceResumingEvent;
                }
        ));
    }

    public void suspendInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                SUSPEND_INSTANCE,
                i -> {
                    i.setState(SUSPENDING);
                    return i;
                },
                i -> {
                    LOG.info("Suspending instance {}", instanceId);
                    return instanceEvent(InstanceSuspendingEvent.class, i);
                }
        ));
    }

    public void deleteInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
                DELETE_INSTANCE,
                i -> {
                    i.setState(DELETING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for removal", instanceId);
                    return instanceEvent(InstanceDeletingEvent.class, i);
                }
        ));
    }

    private void whenInstanceExists(String instanceId, OperationType operationType, Function<Instance, Instance> instanceProcessor, Function<Instance, InstanceEvent> eventProvider) {
        Optional<Instance> instanceCandidate = instancesAware.getInstance(instanceId);
        if (instanceCandidate.isPresent()) {
            Instance instance = instanceCandidate.get();
            Predicate<Instance> instancePredicate = getOperationPredicate(operationType);
            if (instancePredicate.test(instance)) {
                InstanceEvent event = eventProvider.apply(instance);
                Instance updatedInstance = instanceProcessor.apply(instance);
                instancesAware.saveInstance(updatedInstance);
                producer.produce(message(instance.getCloudType(), event, maxRetries));
            } else {
                LOG.warn(
                        "Skipping instance {} as \"{}\" operation is not supported for cloud {}",
                        instanceId,
                        operationType.value(),
                        instance.getCloudType()
                );
            }
        } else {
            LOG.info("Instance {} not found", instanceId);
        }
    }

    private boolean ifImageExists(String imageId, Consumer<Image> action) {
        Optional<Image> imageCandidate = imagesAware.getImage(imageId);
        if (imageCandidate.isPresent()) {
            action.accept(imageCandidate.get());
            return true;
        } else {
            LOG.info("Image {} not found", imageId);
            return false;
        }
    }

    private boolean ifFlavorExists(String flavorId, Consumer<Flavor> action) {
        Optional<Flavor> flavorCandidate = projectsAware.getProjects().stream()
                .flatMap(p -> p.getFlavors().stream())
                .filter(f -> flavorId.equals(f.getId()))
                .findFirst();
        if (flavorCandidate.isPresent()) {
            action.accept(flavorCandidate.get());
            return true;
        } else {
            LOG.info("Flavor {} not found", flavorId);
            return false;
        }
    }

    private Predicate<Instance> getOperationPredicate(OperationType operationType) {
        return instance -> operationsRegistry.getOperationTypes(instance.getCloudType()).contains(operationType);
    }

}
