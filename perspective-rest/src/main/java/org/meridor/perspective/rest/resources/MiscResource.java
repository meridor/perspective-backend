package org.meridor.perspective.rest.resources;

import org.meridor.perspective.framework.storage.Storage;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.QueryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public QueryResult getServerVersion() {
        Optional<String> versionCandidate = Optional.ofNullable(getClass().getPackage().getImplementationVersion());
        String version = versionCandidate.isPresent() ? versionCandidate.get() : "unknown";
        DataContainer dataContainer = new DataContainer(Collections.singletonList(SERVER_VERSION));
        dataContainer.addRow(Collections.singletonList(version));
        QueryResult queryResult = new QueryResult();
        queryResult.setCount(1);
        queryResult.setStatus(QueryStatus.SUCCESS);
        queryResult.setData(dataContainer.toData());
        return queryResult;
    }

}
