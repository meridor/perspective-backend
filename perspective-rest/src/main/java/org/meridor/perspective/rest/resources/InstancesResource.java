package org.meridor.perspective.rest.resources;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Component
@Path("/project/{projectId}/region/{regionId}/instances")
public class InstancesResource {

    @Autowired
    private Storage storage;

    @Produce(ref = "instances")
    private ProducerTemplate producer;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/list")
    public List<Instance> getInstances(@PathParam("projectId") String projectId, @PathParam("regionId") String regionId) {
        return storage.getInstances(projectId, regionId);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/{instanceId}")
    public Response getInstance(@PathParam("projectId") String projectId, @PathParam("regionId") String regionId, @PathParam("instanceId") String instanceId) {
        Optional<Instance> instance = storage.getInstance(projectId, regionId, instanceId);
        return instance.isPresent() ?
                Response.ok(instance.get()).build() :
                Response.status(Response.Status.NOT_FOUND).build();
    }
    
    @DELETE
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteInstances(@PathParam("projectId") String projectId, @PathParam("regionId") String regionId, List<Instance> instances) {
        //TODO: to be implemented!
        return Response.accepted().build();
    }
    
}
