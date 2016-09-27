package org.meridor.perspective.shell.common.repository.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.shell.common.repository.InstancesRepository;
import org.meridor.perspective.shell.common.request.AddInstancesRequest;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindInstancesResult;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.function.Function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.MetadataKey.REGION;
import static org.meridor.perspective.shell.common.repository.impl.MockQueryResults.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@ContextConfiguration(locations = "/META-INF/spring/repository-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = AFTER_CLASS)
public class InstancesRepositoryImplTest {

    @Autowired
    private MockQueryRepository mockQueryRepository;

    @Autowired
    private RequestProvider requestProvider;

    @Autowired
    private InstancesRepository instancesRepository;

    @Test
    public void testFindInstances() {
        Project project = EntityGenerator.getProject();
        Instance instance = EntityGenerator.getInstance();
        Image image = EntityGenerator.getImage();
        Flavor flavor = EntityGenerator.getFlavor();
        QueryResult queryResult = createMockInstancesResult(project, instance, image, flavor);
        mockQueryRepository.addQueryResult(queryResult);
        FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class);
        List<FindInstancesResult> instances = instancesRepository.findInstances(findInstancesRequest);
        assertThat(instances, hasSize(1));
        FindInstancesResult result = instances.get(0);
        assertThat(result.getId(), equalTo(instance.getId()));
        assertThat(result.getRealId(), equalTo(instance.getRealId()));
        assertThat(result.getName(), equalTo(instance.getName()));
        assertThat(result.getProjectId(), equalTo(project.getId()));
        assertThat(result.getProjectName(), equalTo(project.getName()));
        assertThat(result.getCloudId(), equalTo(project.getCloudId()));
        assertThat(result.getCloudType(), equalTo(project.getCloudType()));
        assertThat(result.getImageName(), equalTo(image.getName()));
        assertThat(result.getFlavorName(), equalTo(flavor.getName()));
        assertThat(result.getAddresses(), equalTo(instance.getAddresses().get(0)));
        assertThat(result.getState(), equalTo(instance.getState().value()));
        assertThat(result.getLastUpdated(), is(notNullValue()));

    }

    @Test
    public void testGetInstanceMetadata() {
        Project project = EntityGenerator.getProject();
        Instance instance = EntityGenerator.getInstance();
        Image image = EntityGenerator.getImage();
        Flavor flavor = EntityGenerator.getFlavor();
        mockQueryRepository.addQueryResult(createMockInstancesResult(project, instance, image, flavor));
        mockQueryRepository.addQueryResult(createMockInstanceMetadataResult(instance));
        FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class);
        Map<String, Map<String, String>> instancesMetadata = instancesRepository.getInstancesMetadata(findInstancesRequest);
        assertThat(instancesMetadata.keySet(), contains(instance.getId()));
        Map<String, String> metadata = instancesMetadata.get(instance.getId());
        assertThat(metadata, equalTo(Collections.singletonMap(
                REGION.value(),
                instance.getMetadata().get(REGION)
        )));
    }

    @Test
    public void testAddInstances() {
        Project project = EntityGenerator.getProject();
        Instance instance = EntityGenerator.getInstance();
        Image image = EntityGenerator.getImage();
        Flavor flavor = EntityGenerator.getFlavor();
        Network network = EntityGenerator.getNetwork();
        Keypair keypair = EntityGenerator.getKeypair();
        mockQueryRepository.addQueryResult(createMockProjectsResult(project));
        mockQueryRepository.addQueryResult(createMockFlavorsResult(project, flavor));
        mockQueryRepository.addQueryResult(createMockImagesResult(project, image));
        mockQueryRepository.addQueryResult(createMockNetworksResult(project, network));
        AddInstancesRequest addInstancesRequest = requestProvider.get(AddInstancesRequest.class)
                .withName(instance.getName())
                .withImage(image.getName())
                .withFlavor(flavor.getName())
                .withNetwork(network.getName())
                .withKeypair(keypair.getName())
                .withProject(project.getName());
        List<Instance> instances = addInstancesRequest.getPayload();
        assertThat(instancesRepository.addInstances(instances), is(empty()));

    }

    @Test
    public void testDeleteInstances() {
        testModificationOperation(instancesRepository::deleteInstances);
    }

    @Test
    public void testStartInstances() {
        testModificationOperation(instancesRepository::startInstances);
    }

    @Test
    public void testShutdownInstances() {
        testModificationOperation(instancesRepository::shutdownInstances);
    }

    @Test
    public void testPauseInstances() {
        testModificationOperation(instancesRepository::pauseInstances);
    }

    @Test
    public void testSuspendInstances() {
        testModificationOperation(instancesRepository::suspendInstances);
    }

    @Test
    public void testResumeInstances() {
        testModificationOperation(instancesRepository::resumeInstances);
    }

    @Test
    public void testRebuildInstances() {
        assertThat(instancesRepository.rebuildInstances("test-image", Collections.singleton("test-id")), is(empty()));
    }

    @Test
    public void testResizeInstances() {
        assertThat(instancesRepository.resizeInstances("test-flavor", Collections.singleton("test-id")), is(empty()));
    }

    @Test
    public void testRebootInstances() {
        testModificationOperation(instancesRepository::rebootInstances);
    }

    @Test
    public void testHardRebootInstances() {
        testModificationOperation(instancesRepository::hardRebootInstances);
    }

    private void testModificationOperation(Function<Collection<String>, Set<String>> operation) {
        assertThat(operation.apply(Collections.singleton("test-id")), is(empty()));
    }

}