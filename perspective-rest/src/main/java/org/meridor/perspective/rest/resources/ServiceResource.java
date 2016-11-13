package org.meridor.perspective.rest.resources;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.backend.storage.OperationsRegistry;
import org.meridor.perspective.backend.storage.Storage;
import org.meridor.perspective.rest.handler.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.meridor.perspective.rest.handler.Response.ok;
import static org.meridor.perspective.rest.handler.Response.serviceUnavailable;

@Component
@Path("/")
public class ServiceResource {

    private final Storage storage;

    private final OperationsRegistry operationsRegistry;

    @Autowired
    public ServiceResource(Storage storage, OperationsRegistry operationsRegistry) {
        this.storage = storage;
        this.operationsRegistry = operationsRegistry;
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

    @GET
    @Path("/operations")
    @Produces(APPLICATION_JSON)
    public Response getSupportedOperations() {
        Map<String, Set<OperationType>> supportedOperations = Arrays.stream(CloudType.values())
                .filter(ct -> !operationsRegistry.getOperationTypes(ct).isEmpty())
                .collect(Collectors.toMap(
                        CloudType::value,
                        operationsRegistry::getOperationTypes
                ));
        return ok(supportedOperations);
    }

}
