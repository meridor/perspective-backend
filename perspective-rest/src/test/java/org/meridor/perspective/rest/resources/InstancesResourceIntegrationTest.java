package org.meridor.perspective.rest.resources;

import org.junit.Test;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.mock.EntityGenerator;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class InstancesResourceIntegrationTest extends BaseIntegrationTest {
    
    @Test
    public void testList() {
        List<Instance> instances = target("/cloud/mock/project/test-project/region/test-region/instance/list")
                .request()
                .get(new GenericType<List<Instance>>() {
                });
        assertThat(instances, hasSize(1));
        assertThat(instances.get(0), equalTo(EntityGenerator.getInstance()));
    }
    
    @Test
    public void testMissingList() {
        List<Instance> instances = target("/cloud/mock/project/missing-project/region/missing-region/instance/list")
                .request()
                .get(new GenericType<List<Instance>>() {
                });
        assertThat(instances, empty());
    }
    
    @Test
    public void testGetById() {
        Instance instance = target("/cloud/mock/project/missing-project/region/missing-region/instance/test-instance")
                .request()
                .get(Instance.class);
        assertThat(instance, equalTo(EntityGenerator.getInstance()));
    }

    @Test
    public void testGetByMissingId() {
        Response response = target("/cloud/mock/project/missing-project/regions/missing-region/instance/missing-id")
                .request()
                .get();
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }
    
}
