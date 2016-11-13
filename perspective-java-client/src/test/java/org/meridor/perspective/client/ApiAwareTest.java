package org.meridor.perspective.client;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ApiAwareTest {
    
    @Test
    public void testGet() {
        ApiAware apiAware = ApiAware.withUrl("http://example.com/");
        assertThat(apiAware.get(InstancesApi.class), is(instanceOf(InstancesApi.class)));
        assertThat(apiAware.get(ImagesApi.class), is(instanceOf(ImagesApi.class)));
        assertThat(apiAware.get(QueryApi.class), is(instanceOf(QueryApi.class)));
        assertThat(apiAware.get(ServiceApi.class), is(instanceOf(ServiceApi.class)));
    }

    @Test
    public void testGetWebSocketUrl() {
        assertThat(
                ApiAware.withUrl("http://example.com/").getWebSocketUrl("test"),
                equalTo("ws://example.com/test")
        );
        assertThat(
                ApiAware.withUrl("http://example.com:8080/").getWebSocketUrl("test"),
                equalTo("ws://example.com:8080/test")
        );
        assertThat(
                ApiAware.withUrl("http://192.168.0.101:8080/").getWebSocketUrl("test"),
                equalTo("ws://192.168.0.101:8080/test")
        );
    }

}