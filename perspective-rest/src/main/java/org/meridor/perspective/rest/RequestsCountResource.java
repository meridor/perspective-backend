package org.meridor.perspective.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.MainInput;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.Clock;

@Path("/")
public class RequestsCountResource {

    private static final Logger LOG = LoggerFactory.getLogger(RequestsCountResource.class);
    
    @MainInput
    private EventProducer eventProducer;
    
    @GET
    @Path("/count")
    public Response getCount() {
        Long timestamp = Clock.systemDefaultZone().instant().getEpochSecond();
        LOG.info("Received count request at %l", timestamp);
        eventProducer.produce(new Event(timestamp.toString()));
        return Response.ok().build();
    }
    
}
