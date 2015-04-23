package org.meridor.perspective.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.MainInput;
import ru.yandex.qatools.camelot.core.ProcessingEngine;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.Clock;

@Path("/")
public class RequestsCountResource {

    private static final Logger LOG = LoggerFactory.getLogger(RequestsCountResource.class);

    @Autowired
    private ProcessingEngine processingEngine;
    
    @GET
    @Path("/count")
    public Response getCount() {
        Long timestamp = Clock.systemDefaultZone().instant().getEpochSecond();
        LOG.info("Received count request at {}", timestamp);
        getEventProducer().produce(new CountEvent(timestamp));
        return Response.ok().build();
    }
    
    private EventProducer getEventProducer() {
        return processingEngine.getPlugin(RequestsCountAggregator.class).getContext().getInput();
    }
    
}
