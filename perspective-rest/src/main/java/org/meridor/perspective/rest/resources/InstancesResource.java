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
    @Path("/start")
    @Consumes(APPLICATION_JSON)
    public Response startInstances(List<String> instanceIds) {
        instancesService.startInstances(instanceIds);
        return ok();
    }

    @PUT
    @Path("/shutdown")
    @Consumes(APPLICATION_JSON)
    public Response shutdownInstances(List<String> instanceIds) {
        instancesService.shutdownInstances(instanceIds);
        return ok();
    }

    @PUT
    @Path("/pause")
    @Consumes(APPLICATION_JSON)
    public Response pauseInstances(List<String> instanceIds) {
        instancesService.pauseInstances(instanceIds);
        return ok();
    }

    @PUT
    @Path("/resume")
    @Consumes(APPLICATION_JSON)
    public Response resumeInstances(List<String> instanceIds) {
        instancesService.resumeInstances(instanceIds);
        return ok();
    }

    @PUT
    @Path("/suspend")
    @Consumes(APPLICATION_JSON)
    public Response suspendInstances(List<String> instanceIds) {
        instancesService.suspendInstances(instanceIds);
        return ok();
    }

    @PUT
    @Path("/resize/{flavorId}")
    @Consumes(APPLICATION_JSON)
    public Response resizeInstances(@PathParam("flavorId") String flavorId, List<String> instanceIds) {
        boolean flavorExists = instancesService.resizeInstances(flavorId, instanceIds);
        return flavorExists ?
                ok() : notFound(String.format("Flavor %s does not exist", flavorId));
    }

    @PUT
    @Path("/rebuild/{imageId}")
    @Consumes(APPLICATION_JSON)
    public Response rebuildInstances(@PathParam("imageId") String imageId, List<String> instanceIds) {
        boolean imageExists = instancesService.rebuildInstances(imageId, instanceIds);
        return imageExists ?
                ok() : notFound(String.format("Image %s does not exist", imageId));
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
