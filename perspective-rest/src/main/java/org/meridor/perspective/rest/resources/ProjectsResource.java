package org.meridor.perspective.rest.resources;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.CloudConfigurationProvider;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;

@Component
@Path("/projects")
public class ProjectsResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsResource.class);
    
    @Autowired
    private Storage storage;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects() {
        LOG.info("Getting projects list");
        Collection<Project> projectsList = new ArrayList<>();
        for (CloudType cloudType : cloudConfigurationProvider.getCloudTypes()) {
            Collection<Project> projects = storage.getProjects(cloudType);
            projectsList.addAll(projects);
        }
        return Response.ok(projectsList).build();
    }
    
}
