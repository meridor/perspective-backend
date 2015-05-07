package org.meridor.perspective.rest.resources;

import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("/instances")
public class InstancesResource {

    @Autowired
    private HazelcastInstance hazelcastClient;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/all")
    public List<Project> getProjects() {
        return (List<Project>) hazelcastClient.getMap("projects").get("all");
    }
    
}
