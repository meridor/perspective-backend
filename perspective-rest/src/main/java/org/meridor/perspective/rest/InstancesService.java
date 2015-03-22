package org.meridor.perspective.rest;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/instances")
@Component
public class InstancesService {

    @Produce(uri = "direct:ping")
    private ProducerTemplate ping;

    @GET
    @Path("/ping")
    public Response ping() {
        ping.sendBody("ping");
        return Response.ok().build();
    }


}
