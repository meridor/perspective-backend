package org.meridor.perspective.rest.services;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.events.*;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private InstancesAware instancesAware;

    @Autowired
    private ProjectsAware projectsAware;

    @Destination(WRITE_TASKS)
    private Producer producer;
    
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
            producer.produce(message(instance.getCloudType(), event));
        }
    }
    
    public void rebootInstances(List<String> instanceIds) {
        for (String instanceId : instanceIds) {
            whenInstanceExists(
                    instanceId,
                    i -> {
                        i.setState(REBOOTING);
                        return i;
                    },
                    i -> {
                        LOG.info("Queuing instance {} for reboot", instanceId);
                        return instanceEvent(InstanceRebootingEvent.class, i);
                    }
            );
        }
    }
    
    public void hardRebootInstances(List<String> instanceIds) {
        for (String instanceId : instanceIds) {
            whenInstanceExists(
                    instanceId,
                    i -> {
                        i.setState(HARD_REBOOTING);
                        return i;
                    },
                    i -> {
                        LOG.info("Queuing instance {} for hard reboot", instanceId);
                        return instanceEvent(InstanceHardRebootingEvent.class, i);
                    }
            );
        }
    }
    
    public void deleteInstances(List<String> instanceIds) {
        for (String instanceId : instanceIds) {
            whenInstanceExists(
                instanceId,
                    i -> {
                        i.setState(DELETING);
                        return i;
                    },
                    i -> {
                    LOG.info("Queuing instance {} for removal", instanceId);
                    return instanceEvent(InstanceDeletingEvent.class, i);
                }
            );
        }
    }
    
    private void whenInstanceExists(String instanceId, Function<Instance, Instance> instanceProcessor, Function<Instance, InstanceEvent> eventProvider) {
        Optional<Instance> instanceCandidate = instancesAware.getInstance(instanceId);
        if (instanceCandidate.isPresent()) {
            Instance instance = instanceCandidate.get();
            InstanceEvent event = eventProvider.apply(instance);
            Instance updatedInstance = instanceProcessor.apply(instance);
            instancesAware.saveInstance(updatedInstance);
            producer.produce(message(instance.getCloudType(), event));
        } else {
            LOG.info("Instance {} not found", instanceId);
        }
    }

}
