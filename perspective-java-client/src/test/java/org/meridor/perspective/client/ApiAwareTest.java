package org.meridor.perspective.client;

import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
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

}