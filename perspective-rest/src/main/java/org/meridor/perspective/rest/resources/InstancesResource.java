package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.services.InstancesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static org.meridor.perspective.rest.resources.ResponseUtils.notFound;

@Component
@Path("/instances")
public class InstancesResource {

    @Autowired
    private InstancesService instancesService;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{instanceId}")
    public Response getInstanceById(@PathParam("instanceId") String instanceId) {
        Optional<Instance> instance = instancesService.getInstanceById(instanceId);
        return instance.isPresent() ?
                Response.ok(instance.get()).build() :
                notFound(String.format("Instance with id = %s not found", instanceId));
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response launchInstances(List<Instance> instances) {
        instancesService.launchInstances(instances);
        return Response.ok().build();
    }

    @PUT
    @Path("/reboot")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response rebootInstances(List<String> instanceIds) {
        instancesService.rebootInstances(instanceIds);
        return Response.ok().build();
    }

    @PUT
    @Path("/hard-reboot")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response hardRebootInstances(List<String> instanceIds) {
        instancesService.hardRebootInstances(instanceIds);
        return Response.ok().build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteInstances(List<String> instanceIds) {
        instancesService.deleteInstances(instanceIds);
        return Response.ok().build();
    }

}
