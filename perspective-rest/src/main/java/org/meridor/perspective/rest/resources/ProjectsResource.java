package org.meridor.perspective.rest.resources;

import org.meridor.perspective.rest.storage.IllegalQueryException;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Component
@Path("/projects")
public class ProjectsResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsResource.class);
    
    @Autowired
    private Storage storage;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects(@QueryParam("query") String query) {
        try {
            LOG.info("Getting projects list");
            return Response.ok(storage.getProjects(Optional.ofNullable(query))).build();
        } catch (IllegalQueryException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
}
