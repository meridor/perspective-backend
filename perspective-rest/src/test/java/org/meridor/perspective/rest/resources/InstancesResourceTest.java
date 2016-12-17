package org.meridor.perspective.rest.resources;

import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.client.InstancesApi;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.backend.storage.OperationsRegistry;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class InstancesResourceTest extends BaseResourceTest<InstancesApi> {

    @Autowired
    private InstancesAware instancesAware;

    @Autowired
    private ImagesAware imagesAware;

    @Autowired
    private ProjectsAware projectsAware;

    @Autowired
    private OperationsRegistry operationsRegistry;
    
    @Before
    public void before() {
        instancesAware.saveInstance(EntityGenerator.getInstance());
        imagesAware.saveImage(EntityGenerator.getImage());
        projectsAware.saveProject(EntityGenerator.getProject());
        Arrays.stream(OperationType.values())
                .forEach(ot -> operationsRegistry.addOperation(MOCK, ot));
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
    public void testStartInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.start(getInstanceIds()));
    }

    @Test
    public void testShutdownInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.shutdown(getInstanceIds()));
    }

    @Test
    public void testPauseInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.pause(getInstanceIds()));
    }

    @Test
    public void testResumeInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.resume(getInstanceIds()));
    }

    @Test
    public void testSuspendInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.suspend(getInstanceIds()));
    }

    @Test
    public void testResizeInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.resize(
                EntityGenerator.getFlavor().getId(),
                getInstanceIds()
        ));
    }

    @Test
    public void testResizeInstancesMissingFlavor() throws Exception {
        Response<ResponseBody> response = getApi().resize("missing-id", getInstanceIds()).execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    public void testRebuildInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.rebuild(
                EntityGenerator.getImage().getId(),
                getInstanceIds()
        ));
    }

    @Test
    public void testRenameInstance() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.rename(
                Collections.singletonMap(
                        EntityGenerator.getInstance().getId(),
                        "some-new-name"
                )
        ));
    }

    @Test
    public void testRebuildInstancesMissingImage() throws Exception {
        Response<ResponseBody> response = getApi().rebuild("missing-id", getInstanceIds()).execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    public void testRebootInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.reboot(getInstanceIds()));
    }
    
    @Test
    public void testHardRebootInstances() throws Exception {
        testResponseIsSuccessful(instancesApi -> instancesApi.hardReboot(getInstanceIds()));
    }

    private void testResponseIsSuccessful(Function<InstancesApi, Call<ResponseBody>> action) throws Exception {
        Response<ResponseBody> response = action.apply(getApi()).execute();
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
