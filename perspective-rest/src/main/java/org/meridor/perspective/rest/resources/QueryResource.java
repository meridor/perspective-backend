package org.meridor.perspective.rest.resources;

import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryProcessor;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("/query")
public class QueryResource {

    @Autowired
    private QueryProcessor queryProcessor;
    
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<QueryResult> selectQuery(Query query) {
        return queryProcessor.process(query);
    }

}
