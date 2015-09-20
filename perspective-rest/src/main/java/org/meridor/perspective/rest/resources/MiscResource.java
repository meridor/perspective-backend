package org.meridor.perspective.rest.resources;

import org.meridor.perspective.framework.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/")
public class MiscResource {

    @Autowired
    private Storage storage;

    @GET
    @Path("/ping")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects() {
        return storage.isAvailable() ? 
                Response.ok().build() :
                Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

}
