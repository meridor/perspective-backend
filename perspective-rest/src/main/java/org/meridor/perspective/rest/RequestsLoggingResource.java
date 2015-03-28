package org.meridor.perspective.rest;

import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.MainInput;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/")
public class RequestsLoggingResource {

    @MainInput
    private EventProducer input;
    
    private RequestsCountProcessor requestsCountProcessor;
    
    private AtomicInteger currentValue = new AtomicInteger(0);
    
    @GET
    @Path("/count")
    public Response getCount() {
        requestsCountProcessor.process(currentValue);
        return Response.ok(currentValue.get()).build();
    }
    
}
