package org.meridor.perspective.rest.resources;

import org.meridor.perspective.framework.storage.Storage;
import org.meridor.perspective.rest.handler.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.meridor.perspective.rest.handler.Response.ok;
import static org.meridor.perspective.rest.handler.Response.serviceUnavailable;

@Component
@Path("/")
public class ServiceResource {

    private final Storage storage;

    @Autowired
    public ServiceResource(Storage storage) {
        this.storage = storage;
    }

    @GET
    @Path("/ping")
    public Response ping() {
        return storage.isAvailable() ?
                ok() :
                serviceUnavailable();
    }

    @GET
    @Path("/version")
    @Produces(TEXT_PLAIN)
    public String getServerVersion() {
        Optional<String> versionCandidate = Optional.ofNullable(getClass().getPackage().getImplementationVersion());
        return versionCandidate.isPresent() ? versionCandidate.get() : "unknown";
    }

}
