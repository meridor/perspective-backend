package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("/projects")
public class ProjectsResource {

    @Autowired
    private Storage storage;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/list")
    public List<Project> getProjects() {
        return storage.getProjects();
    }
    
}
