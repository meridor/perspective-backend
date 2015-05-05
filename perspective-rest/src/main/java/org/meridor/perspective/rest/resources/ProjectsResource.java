package org.meridor.perspective.rest.resources;

import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Projects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/projects")
public class ProjectsResource {

    @Autowired
    private HazelcastInstance hazelcastClient;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProjects() {
        Projects projects = (Projects) hazelcastClient.getMap("projects").get("current");
        return Response.ok(projects).build();
    }
    
}
