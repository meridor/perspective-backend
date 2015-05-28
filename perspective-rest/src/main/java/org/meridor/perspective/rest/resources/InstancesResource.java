package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.events.InstanceQueuedEvent;
import org.meridor.perspective.rest.storage.Destination;
import org.meridor.perspective.rest.storage.Producer;
import org.meridor.perspective.rest.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static org.meridor.perspective.beans.DestinationName.INSTANCES;
import static org.meridor.perspective.events.EventFactory.instanceEvent;
import static org.meridor.perspective.framework.Util.getUUID;

@Component
@Path("/cloud/{cloudType}/project/{projectId}/region/{regionId}/instance")
public class InstancesResource {

    @Autowired
    private Storage storage;
    
    @Destination(INSTANCES)
    private Producer producer;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/list")
    public Response getInstances(@PathParam("cloudType") String cloudTypeString, @PathParam("projectId") String projectId, @PathParam("regionId") String regionId) {
        try {
            CloudType cloudType = CloudType.fromValue(cloudTypeString);
            return Response.ok(storage.getInstances(cloudType, projectId, regionId)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{instanceId}")
    public Response getInstance(@PathParam("cloudType") String cloudTypeString, @PathParam("projectId") String projectId, @PathParam("regionId") String regionId, @PathParam("instanceId") String instanceId) {
        try {
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
    public Response launchInstances(@PathParam("cloudType") String cloudTypeString, @PathParam("projectId") String projectId, @PathParam("regionId") String regionId, List<Instance> instances) {
        CloudType cloudType = CloudType.fromValue(cloudTypeString);
        try {
            for (Instance instance : instances) {
                instance.setId(getUUID());
                InstanceQueuedEvent instanceQueuedEvent = instanceEvent(InstanceQueuedEvent.class, cloudType, instance);
                producer.produce(instanceQueuedEvent);
            }
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteInstances(@PathParam("cloudType") String cloudTypeString, @PathParam("projectId") String projectId, @PathParam("regionId") String regionId, List<Instance> instances) {
        CloudType cloudType = CloudType.fromValue(cloudTypeString);
        //TODO: to be implemented!
        return Response.accepted().build();
    }
    
}
