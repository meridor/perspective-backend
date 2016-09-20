package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.handler.Response;
import org.meridor.perspective.rest.services.InstancesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.meridor.perspective.rest.handler.Response.notFound;
import static org.meridor.perspective.rest.handler.Response.ok;

@Component
@Path("/instances")
public class InstancesResource {

    private final InstancesService instancesService;

    @Autowired
    public InstancesResource(InstancesService instancesService) {
        this.instancesService = instancesService;
    }
    
    @GET
    @Path("/{instanceId}")
    public Response getInstanceById(@PathParam("instanceId") String instanceId) {
        Optional<Instance> instance = instancesService.getInstanceById(instanceId);
        return instance.isPresent() ?
                ok(instance.get()) :
                notFound(String.format("Instance with id = %s not found", instanceId));
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Response launchInstances(List<Instance> instances) {
        instancesService.launchInstances(instances);
        return ok();
    }

    @PUT
    @Path("/reboot")
    @Consumes(APPLICATION_JSON)
    public Response rebootInstances(List<String> instanceIds) {
        instancesService.rebootInstances(instanceIds);
        return ok();
    }

    @PUT
    @Path("/hard-reboot")
    @Consumes(APPLICATION_JSON)
    public Response hardRebootInstances(List<String> instanceIds) {
        instancesService.hardRebootInstances(instanceIds);
        return ok();
    }

    @POST
    @Path("/delete")
    @Consumes(APPLICATION_JSON)
    public Response deleteInstances(List<String> instanceIds) {
        instancesService.deleteInstances(instanceIds);
        return ok();
    }

}
