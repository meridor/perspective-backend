package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceStatus;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.events.InstanceDeletingEvent;
import org.meridor.perspective.events.InstanceHardRebootingEvent;
import org.meridor.perspective.events.InstanceLaunchingEvent;
import org.meridor.perspective.events.InstanceRebootingEvent;
import org.meridor.perspective.rest.storage.Destination;
import org.meridor.perspective.rest.storage.Producer;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static org.meridor.perspective.beans.DestinationName.INSTANCES;
import static org.meridor.perspective.events.EventFactory.instanceEvent;
import static org.meridor.perspective.events.EventFactory.now;
import static org.meridor.perspective.events.EventFactory.uuid;

@Component
@Path("/{cloudType}/project/{projectId}/region/{regionId}/instance")
public class InstancesResource {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesResource.class);
    
    @Autowired
    private Storage storage;
    
    @Destination(INSTANCES)
    private Producer producer;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/list")
    public Response getInstances(
            @PathParam("cloudType") String cloudTypeString,
            @PathParam("projectId") String projectId,
            @PathParam("regionId") String regionId
    ) {
        try {
            LOG.info("Getting instances list for cloud = {}, projectId = {}, regionId = {}", cloudTypeString, projectId, regionId);
            CloudType cloudType = CloudType.fromValue(cloudTypeString);
            return Response.ok(storage.getInstances(cloudType, projectId, regionId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{instanceId}")
    public Response getInstance(
            @PathParam("cloudType") String cloudTypeString,
            @PathParam("instanceId") String instanceId
    ) {
        try {
            LOG.info("Getting instance for cloud = {}, instanceId = {}", cloudTypeString, instanceId);
            CloudType cloudType = CloudType.fromValue(cloudTypeString);
            Optional<Instance> instance = storage.getInstance(cloudType, instanceId);
            return instance.isPresent() ?
                    Response.ok(instance.get()).build() :
                    Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response launchInstances(
            @PathParam("cloudType") String cloudTypeString,
            @PathParam("projectId") String projectId,
            @PathParam("regionId") String regionId,
            List<Instance> instances
    ) {
        CloudType cloudType = CloudType.fromValue(cloudTypeString);
        for (Instance instance : instances) {
            LOG.info("Queuing instance {} for launch in cloud = {}, projectId = {}, regionId = {}", instance, cloudTypeString, projectId, regionId);
            instance.setId(uuid());
            instance.setCloudType(cloudType);
            instance.setProjectId(projectId);
            instance.setRegionId(regionId);
            instance.setCreated(now());
            instance.setStatus(InstanceStatus.QUEUED);
            storage.saveInstance(instance);
            InstanceLaunchingEvent event = instanceEvent(InstanceLaunchingEvent.class, instance);
            producer.produce(event);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/reboot")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response rebootInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            LOG.info("Queuing instance {} for reboot", instance);
            InstanceRebootingEvent event = instanceEvent(InstanceRebootingEvent.class, instance);
            producer.produce(event);
        }
        return Response.ok().build();
    }
    
    @PUT
    @Path("/hard-reboot")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response hardRebootInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            LOG.debug("Queuing instance {} for hard reboot", instance);
            InstanceHardRebootingEvent event = instanceEvent(InstanceHardRebootingEvent.class, instance);
            producer.produce(event);
        }
        return Response.ok().build();
    }
    
    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteInstances(List<Instance> instances) {
        for (Instance instance : instances) {
            LOG.debug("Queuing instance {} for removal", instance);
            InstanceDeletingEvent event = instanceEvent(InstanceDeletingEvent.class, instance);
            producer.produce(event);
        }
        return Response.ok().build();
    }
    
}
