package org.meridor.perspective.rest.resources;

import org.meridor.perspective.sql.Parameter;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryProcessor;
import org.meridor.perspective.sql.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.isEmpty;

@Component
@Path("/query")
public class QueryResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(InstancesResource.class);

    @Autowired
    private QueryProcessor queryProcessor;
    
    private final AtomicLong queryCounter = new AtomicLong();
    
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<QueryResult> query(List<Query> queries) {
        long queryId = queryCounter.incrementAndGet();
        List<QueryResult> results = queries.stream().flatMap(q -> {
            LOG.info(
                    "Processing query #{} with sql = \"{}\" and parameters = [{}]",
                    queryId,
                    q.getSql(),
                    q.getParameters().stream().map(Parameter::toString).collect(Collectors.joining(", "))
            );
            return queryProcessor.process(q).stream();
        }).collect(Collectors.toList());
        String queryStatus = results.stream()
                .map(QueryResource::getQueryStatus)
                .collect(Collectors.joining("; "));
        LOG.debug("Query #{} results = [{}]", queryId, queryStatus);
        LOG.trace("Query #{} results raw data = {}", results);
        return results;
    }
    
    private static String getQueryStatus(QueryResult r) {
        return String.format(
                "%d rows, status = %s, message = %s",
                r.getCount(),
                r.getStatus().value(),
                !isEmpty(r.getMessage()) ? r.getMessage() : "<empty>"
        );
    }

}
