package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Component
@Path("/projects/{projectId}/regions/{regionId}/instances")
public class InstancesResource {

    @Autowired
    private Storage storage;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/list")
    public List<Instance> getInstances(@PathParam("projectId") String projectId, @PathParam("regionId") String regionId) {
        return storage.getInstances(projectId, regionId);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{instanceId}")
    public Response getInstance(@PathParam("projectId") String projectId, @PathParam("regionId") String regionId, @PathParam("instanceId") String instanceId) {
        Optional<Instance> instance = storage.getInstance(projectId, regionId, instanceId);
        return instance.isPresent() ?
                Response.ok(instance.get()).build() :
                Response.status(Response.Status.NOT_FOUND).build();
    }
    
}
