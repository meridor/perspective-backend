package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.events.InstanceDeletingEvent;
import org.meridor.perspective.events.InstanceHardRebootingEvent;
import org.meridor.perspective.events.InstanceLaunchingEvent;
import org.meridor.perspective.events.InstanceRebootingEvent;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.framework.storage.IllegalQueryException;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.meridor.perspective.beans.DestinationName.WRITE_TASKS;
import static org.meridor.perspective.events.EventFactory.*;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;
import static org.meridor.perspective.rest.resources.ResponseUtils.clientError;
import static org.meridor.perspective.rest.resources.ResponseUtils.notFound;

@Component
@Path("/instances")
public class InstancesResource {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesResource.class);

    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ProjectsAware projectsAware;

    @Destination(WRITE_TASKS)
    private Producer producer;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getInstances(@QueryParam("query") String query) {
        try {
            LOG.info("Getting instances list for query = {}", query);
            List<Instance> instances = new ArrayList<>(instancesAware.getInstances(Optional.ofNullable(query)));
            return Response.ok(new GenericEntity<List<Instance>>(instances){}).build();
        } catch (IllegalQueryException e) {
            return clientError(String.format("Illegal request %s", query));
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{instanceId}")
    public Response getInstanceById(@PathParam("instanceId") String instanceId) {
        LOG.info("Getting instance for instanceId = {}", instanceId);
        Optional<Instance> instance = instancesAware.getInstance(instanceId);
        return instance.isPresent() ?
                Response.ok(instance.get()).build() :
                notFound(String.format("Instance with id = %s not found", instanceId));
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response launchInstances(List<Instance> instances) {
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
                    return clientError(String.format("Project %s not found", instance.getProjectId()));
                }
            }
            instancesAware.saveInstance(instance);
            InstanceLaunchingEvent event = instanceEvent(InstanceLaunchingEvent.class, instance);
            event.setTemporaryInstanceId(temporaryId);
            producer.produce(message(instance.getCloudType(), event));
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/reboot")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response rebootInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            LOG.info("Queuing instance {} ({}) for reboot", instance.getName(), instance.getId());
            InstanceRebootingEvent event = instanceEvent(InstanceRebootingEvent.class, instance);
            producer.produce(message(instance.getCloudType(), event));
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/hard-reboot")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response hardRebootInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            LOG.debug("Queuing instance {} ({}) for hard reboot", instance.getName(), instance.getId());
            InstanceHardRebootingEvent event = instanceEvent(InstanceHardRebootingEvent.class, instance);
            producer.produce(message(instance.getCloudType(), event));
        }
        return Response.ok().build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            LOG.debug("Queuing instance {} ({}) for removal", instance.getName(), instance.getId());
            instance.setState(InstanceState.DELETING);
            instancesAware.saveInstance(instance);
            InstanceDeletingEvent event = instanceEvent(InstanceDeletingEvent.class, instance);
            producer.produce(message(instance.getCloudType(), event));
        }
        return Response.ok().build();
    }

}
