package org.meridor.perspective.rest.resources;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.rest.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/{cloudType}/project")
public class ProjectsResource {

    @Autowired
    private Storage storage;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/list")
    public Response getProjects(@PathParam("cloudType") String cloudTypeString) {
        try {
            CloudType cloudType = CloudType.fromValue(cloudTypeString);
            return Response.ok(storage.getProjects(cloudType)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
}
