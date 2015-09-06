package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.IllegalQueryException;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Path("/projects")
public class ProjectsResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsResource.class);

    @Autowired
    private ProjectsAware storage;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects(@QueryParam("query") String query) {
        try {
            LOG.info("Getting projects list for query = {}", query);
            List<Project> projects = new ArrayList<>(storage.getProjects(Optional.ofNullable(query)));
            return Response.ok(new GenericEntity<List<Project>>(projects){}).build();
        } catch (IllegalQueryException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

}
