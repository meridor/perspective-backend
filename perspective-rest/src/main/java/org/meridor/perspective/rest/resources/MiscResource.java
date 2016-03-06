package org.meridor.perspective.rest.resources;

import org.meridor.perspective.framework.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Component
@Path("/")
public class MiscResource {
    
    private static final String SERVER_VERSION = "version";

    @Autowired
    private Storage storage;

    @GET
    @Path("/ping")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response ping() {
        return storage.isAvailable() ? 
                Response.ok().build() :
                Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

    @GET
    @Path("/version")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String getServerVersion() {
        Optional<String> versionCandidate = Optional.ofNullable(getClass().getPackage().getImplementationVersion());
        return versionCandidate.isPresent() ? versionCandidate.get() : "unknown";
    }

}
