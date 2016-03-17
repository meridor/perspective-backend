package org.meridor.perspective.rest.resources;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class InstancesResourceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InstancesAware instancesAware;
    
    @Before
    public void before() {
        instancesAware.saveInstance(EntityGenerator.getInstance());
    }
    
    @Test
    public void testGetById() throws Exception {
        Thread.sleep(500);
        Response response = target("/instances/test-instance")
                .request()
                .get();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void testGetByMissingId() {
        Response response = target("/instances/missing-id")
                .request()
                .get();
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testLaunchInstances() throws Exception {
        List<Instance> instances = new ArrayList<>();
        Instance instance = EntityGenerator.getInstance();
        instances.add(instance);
        Entity<List<Instance>> entity = Entity.entity(instances, MediaType.APPLICATION_JSON);
        Response response = target("/instances")
                .request()
                .post(entity);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }
    
    @Test
    public void testRebootInstances() throws Exception {
        Response response = target("/instances/reboot")
                .request()
                .put(getInstanceIdsEntity());

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }
    
    @Test
    public void testHardRebootInstances() throws Exception {
        Response response = target("/instances/hard-reboot")
                .request()
                .put(getInstanceIdsEntity());

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }
    
    private Entity<List<String>> getInstanceIdsEntity() {
        return Entity.entity(Collections.singletonList(EntityGenerator.getInstance().getId()), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testDeleteExistingInstances() throws Exception {
        deleteInstance(EntityGenerator.getInstance());
    }

    @Test
    public void testDeleteMissingInstances() throws Exception {
        Instance missingInstance = EntityGenerator.getInstance();
        missingInstance.setId("missing-id");
        missingInstance.setCloudType(CloudType.MOCK);
        missingInstance.setProjectId("missing-project");
        deleteInstance(missingInstance);
    }

    private void deleteInstance(Instance instance) throws Exception {
        List<String> instances = new ArrayList<String>() {
            {
                add(instance.getId());
            }
        };
        Entity<List<String>> entity = Entity.entity(instances, MediaType.APPLICATION_JSON_TYPE);
        Response deleteResponse = target("/instances/delete")
                .request()
                .post(entity);

        assertThat(deleteResponse.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

}
