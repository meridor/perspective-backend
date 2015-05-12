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
import java.util.List;

@Component
@Path("/projects/{projectId}/regions/{regionId}/instances")
public class InstancesResource {

    @Autowired
    private Storage storage;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/list")
    public List<Instance> listInstances(@PathParam("projectId") String projectId, @PathParam("regionId") String regionId) {
        return storage.getInstances(projectId, regionId);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{instanceId}")
    public Instance listInstances(@PathParam("projectId") String projectId, @PathParam("regionId") String regionId, @PathParam("instanceId") String instanceId) {
        return storage.getInstance(projectId, regionId, instanceId);
    }
    
}
