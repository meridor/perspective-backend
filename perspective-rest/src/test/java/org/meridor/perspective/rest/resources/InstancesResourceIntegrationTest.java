package org.meridor.perspective.rest.resources;

import org.junit.Test;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.mock.EntityGenerator;

import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class InstancesResourceIntegrationTest extends BaseIntegrationTest {
    
    @Test
    public void testList() {
        List<Instance> instances = target("/projects/test-project/regions/test-region/instances/list")
                .request()
                .get(new GenericType<List<Instance>>() {
                });
        assertThat(instances, hasSize(1));
        assertThat(instances.get(0), equalTo(EntityGenerator.getInstance()));
    }
    
}
