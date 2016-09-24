package org.meridor.perspective.rest.services;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.events.*;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.meridor.perspective.beans.DestinationName.WRITE_TASKS;
import static org.meridor.perspective.beans.InstanceState.*;
import static org.meridor.perspective.events.EventFactory.*;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Service
public class InstancesService {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesService.class);

    private final InstancesAware instancesAware;

    private final ImagesAware imagesAware;

    private final ProjectsAware projectsAware;

    @Destination(WRITE_TASKS)
    private Producer producer;
    
    @Value("${perspective.messaging.max.retries}")
    private int maxRetries;

    @Autowired
    public InstancesService(
            InstancesAware instancesAware,
            ImagesAware imagesAware,
            ProjectsAware projectsAware
    ) {
        this.instancesAware = instancesAware;
        this.imagesAware = imagesAware;
        this.projectsAware = projectsAware;
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
                i -> {
                    i.setState(LAUNCHING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for start", instanceId);
                    return instanceEvent(InstanceLaunchingEvent.class, i);
                }
        ));
    }

    public void shutdownInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
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
        return ifImageExists(imageId, () -> instanceIds.forEach(
                instanceId -> whenInstanceExists(
                        instanceId,
                        i -> {
                            i.setState(REBUILDING);
                            return i;
                        },
                        i -> {
                            LOG.info("Queuing instance {} for rebuild", instanceId);
                            return instanceEvent(InstanceRebuildingEvent.class, i);
                        }
                )
        ));
    }

    public boolean resizeInstances(String flavorId, List<String> instanceIds) {
        return ifFlavorExists(flavorId, () -> instanceIds.forEach(
                instanceId -> whenInstanceExists(
                        instanceId,
                        i -> {
                            i.setState(RESIZING);
                            return i;
                        },
                        i -> {
                            LOG.info("Queuing instance {} for resize", instanceId);
                            return instanceEvent(InstanceResizingEvent.class, i);
                        }
                )
        ));
    }

    public void pauseInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
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
                i -> {
                    i.setState(RESUMING);
                    return i;
                },
                i -> {
                    LOG.info("Queuing instance {} for resume", instanceId);
                    return instanceEvent(InstanceResumingEvent.class, i);
                }
        ));
    }

    public void suspendInstances(List<String> instanceIds) {
        instanceIds.forEach(instanceId -> whenInstanceExists(
                instanceId,
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

    private void whenInstanceExists(String instanceId, Function<Instance, Instance> instanceProcessor, Function<Instance, InstanceEvent> eventProvider) {
        Optional<Instance> instanceCandidate = instancesAware.getInstance(instanceId);
        if (instanceCandidate.isPresent()) {
            Instance instance = instanceCandidate.get();
            InstanceEvent event = eventProvider.apply(instance);
            Instance updatedInstance = instanceProcessor.apply(instance);
            instancesAware.saveInstance(updatedInstance);
            producer.produce(message(instance.getCloudType(), event, maxRetries));
        } else {
            LOG.info("Instance {} not found", instanceId);
        }
    }

    private boolean ifImageExists(String imageId, Runnable action) {
        Optional<Image> imageCandidate = imagesAware.getImage(imageId);
        if (imageCandidate.isPresent()) {
            action.run();
            return true;
        } else {
            LOG.info("Image {} not found", imageId);
            return false;
        }
    }

    private boolean ifFlavorExists(String flavorId, Runnable action) {
        Optional<Flavor> flavorCandidate = projectsAware.getProjects().stream()
                .flatMap(p -> p.getFlavors().stream())
                .filter(f -> flavorId.equals(f.getId()))
                .findFirst();
        if (flavorCandidate.isPresent()) {
            action.run();
            return true;
        } else {
            LOG.info("Flavor {} not found", flavorId);
            return false;
        }
    }

}
