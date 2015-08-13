package org.meridor.perspective.rest.resources;

import org.junit.Test;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.mock.EntityGenerator;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.meridor.perspective.rest.resources.ListContainsElements.containsElements;

public class InstancesResourceIntegrationTest extends BaseIntegrationTest {
    
    @Test
    public void testList() {
        List<Instance> instances = listInstances();
        assertThat(instances, hasSize(1));
        assertThat(instances.get(0), equalTo(EntityGenerator.getInstance()));
    }
    
    @Test
    public void testMissingList() throws Exception {
        Thread.sleep(500);
        List<Instance> instances = target("/instances")
                .request()
                .get(new GenericType<List<Instance>>() {
                });
        assertThat(instances, empty());
    }
    
    @Test
    public void testGetById() throws Exception {
        Thread.sleep(500);
        Instance instance = target("/instances/test-instance")
                .request()
                .get(Instance.class);
        assertThat(instance, equalTo(EntityGenerator.getInstance()));
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
        instance.setName("new-instance");
        instance.setCloudType(CloudType.MOCK);
        instance.setProjectId("test-project");
        instances.add(instance);
        Entity<List<Instance>> entity = Entity.entity(instances, MediaType.APPLICATION_JSON);
        Response response = target("/instance")
                .request()
                .post(entity);
        
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        
        Thread.sleep(500);
        List<Instance> existingInstances = listInstances();
        assertThat(existingInstances, hasSize(2));
        assertThat(existingInstances, containsElements(i -> i.getName().equals("new-instance")));
    }
    
    @Test
    public void testDeleteExistingInstances() throws Exception {
        deleteInstance(EntityGenerator.getInstance());
        
        Thread.sleep(500);
        List<Instance> currentInstances = listInstances();
        assertThat(currentInstances, empty());
    }

    @Test
    public void testDeleteMissingInstances() throws Exception {
        Instance missingInstance = EntityGenerator.getInstance();
        missingInstance.setId("missing-id");
        missingInstance.setCloudType(CloudType.MOCK);
        missingInstance.setProjectId("missing-project");
        deleteInstance(missingInstance);

        Thread.sleep(500);
        List<Instance> currentInstances = listInstances();
        assertThat(currentInstances, hasSize(1));
        assertThat(currentInstances.get(0), equalTo(EntityGenerator.getInstance()));
    }
    
    private void deleteInstance(Instance instance) throws Exception {
        List<Instance> instances = new ArrayList<Instance>(){
            {
                add(instance);
            }
        };
        Entity<List<Instance>> entity = Entity.entity(instances, MediaType.APPLICATION_JSON_TYPE);
        Response deleteResponse = target("/instances/delete")
                .request()
                .post(entity);

        assertThat(deleteResponse.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }
    
    private List<Instance> listInstances() {
        return target("/instances")
                .request()
                .get(new GenericType<List<Instance>>() {
                });
    }
    
}
