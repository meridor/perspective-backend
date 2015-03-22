package org.meridor.perspective.rest;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Component
public class RootService {

    @Produce(uri = "direct:ping")
    private ProducerTemplate ping;

    @GET
    @Path("/ping")
    @Produces({MediaType.TEXT_PLAIN})
    public Response ping() {
        ping.sendBody("ping");
        return Response.ok().build();
    }

    //TODO: openstack plugin module skeleton
    //TODO: rest projects service
    //TODO: rest images service
    //TODO: rest instances service
    //TODO: openstack project operations impl

}
