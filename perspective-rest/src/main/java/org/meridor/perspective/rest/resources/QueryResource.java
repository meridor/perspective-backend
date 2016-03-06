package org.meridor.perspective.rest.resources;

import org.meridor.perspective.sql.Parameter;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryProcessor;
import org.meridor.perspective.sql.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/query")
public class QueryResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(InstancesResource.class);

    @Autowired
    private QueryProcessor queryProcessor;
    
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<QueryResult> query(List<Query> queries) {
        return queries.stream().flatMap(q -> {
            LOG.info(
                    "Processing query = \"{}\" with parameters = [{}]",
                    q.getSql(),
                    q.getParameters().stream().map(Parameter::toString).collect(Collectors.joining(", "))
            );
            return queryProcessor.process(q).stream();
        }).collect(Collectors.toList());
    }

}
