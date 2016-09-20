package org.meridor.perspective.rest.resources;

import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.client.InstancesApi;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class InstancesResourceTest extends BaseResourceTest<InstancesApi> {

    @Autowired
    private InstancesAware instancesAware;
    
    @Before
    public void before() {
        instancesAware.saveInstance(EntityGenerator.getInstance());
    }
    
    @Test
    public void testGetById() throws Exception {
        String instanceId = EntityGenerator.getInstance().getId();
        Response<Instance> response = getApi().getById(instanceId).execute();
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.body(), equalTo(EntityGenerator.getInstance()));
    }

    @Test
    public void testGetByMissingId() throws Exception {
        Response<Instance> response = getApi().getById("missing-id").execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    public void testLaunchInstances() throws Exception {
        List<Instance> instances = new ArrayList<>();
        Instance instance = EntityGenerator.getInstance();
        instance.setId("another-id");
        instances.add(instance);
        assertThat(instancesAware.getInstances(), hasSize(1));
        Response<ResponseBody> response = getApi().launch(instances).execute();
        assertThat(response.isSuccessful(), is(true));
        assertThat(instancesAware.getInstances(), hasSize(2));
    }
    
    @Test
    public void testRebootInstances() throws Exception {
        Response<ResponseBody> response = getApi().reboot(getInstanceIds()).execute();
        assertThat(response.isSuccessful(), is(true));
    }
    
    @Test
    public void testHardRebootInstances() throws Exception {
        Response<ResponseBody> response = getApi().hardReboot(getInstanceIds()).execute();
        assertThat(response.isSuccessful(), is(true));
    }

    private List<String> getInstanceIds() {
        return Collections.singletonList(EntityGenerator.getInstance().getId());
    }

    @Test
    public void testDeleteExistingInstances() throws Exception {
        String instanceId = EntityGenerator.getInstance().getId();
        testDeleteInstance(instanceId);
    }

    @Test
    public void testDeleteMissingInstances() throws Exception {
        testDeleteInstance("missing-id");
    }

    private void testDeleteInstance(String instanceId) throws Exception {
        List<String> instanceIds = new ArrayList<String>() {
            {
                add(instanceId);
            }
        };
        Response<ResponseBody> response = getApi().delete(instanceIds).execute();
        assertThat(response.isSuccessful(), is(true));
    }

    @Override
    protected Class<InstancesApi> getApiClass() {
        return InstancesApi.class;
    }
}
