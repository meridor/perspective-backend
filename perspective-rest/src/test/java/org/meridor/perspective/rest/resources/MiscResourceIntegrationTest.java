package org.meridor.perspective.rest.resources;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MiscResourceIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testPing() throws Exception {
        Thread.sleep(500);
        Response response = target("/ping")
                .request()
                .get();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }
    
    @Test
    public void testVersion() throws Exception {
        Thread.sleep(500);
        Response response = target("/version")
                .request()
                .get();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }
    
}
