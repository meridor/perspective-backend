package org.meridor.perspective.shell.common.repository.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.*;
import org.meridor.perspective.shell.common.result.FindFlavorsResult;
import org.meridor.perspective.shell.common.result.FindKeypairsResult;
import org.meridor.perspective.shell.common.result.FindNetworksResult;
import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.meridor.perspective.shell.common.validator.MockQueryRepository;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.QueryUtils.createQueryResult;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@ContextConfiguration(locations = "/META-INF/spring/repository-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = AFTER_CLASS)
public class ProjectsRepositoryImplTest {

    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private MockQueryRepository mockQueryRepository;
    
    @Autowired
    private RequestProvider requestProvider;
    
    @Test
    public void testFindProjects() {
        Project project = EntityGenerator.getProject();
        QueryResult queryResult = createQueryResult(
                Arrays.asList("id", "name", "cloud_id", "cloud_type"),
                Collections.singletonList(Arrays.asList(
                        project.getId(),
                        project.getName(),
                        project.getCloudId(),
                        project.getCloudType().value()
                ))
        );
        mockQueryRepository.setQueryResult(queryResult);
        FindProjectsRequest findProjectsRequest = requestProvider.get(FindProjectsRequest.class);
        List<FindProjectsResult> projects = projectsRepository.findProjects(findProjectsRequest);
        assertThat(projects, hasSize(1));
        FindProjectsResult result = projects.get(0);
        assertThat(result.getId(), equalTo(project.getId()));
        assertThat(result.getName(), equalTo(project.getName()));
        assertThat(result.getCloudId(), equalTo(project.getCloudId()));
        assertThat(result.getCloudType(), equalTo(project.getCloudType()));
    }
    
    @Test
    public void testFindFlavors() {
        Project project = EntityGenerator.getProject();
        Flavor flavor = EntityGenerator.getFlavor();

        QueryResult queryResult = createQueryResult(
                Arrays.asList("flavors.id", "flavors.name", "projects.name", "flavors.vcpus", "flavors.ram", "flavors.root_disk", "flavors.ephemeral_disk"),
                Collections.singletonList(Arrays.asList(
                        flavor.getId(),
                        flavor.getName(),
                        project.getName(),
                        flavor.getVcpus(),
                        flavor.getRam(),
                        flavor.getRootDisk(),
                        flavor.getEphemeralDisk()
                ))
        );
        mockQueryRepository.setQueryResult(queryResult);
        FindFlavorsRequest findFlavorsRequest = requestProvider.get(FindFlavorsRequest.class);
        List<FindFlavorsResult> flavors = projectsRepository.findFlavors(findFlavorsRequest);
        assertThat(flavors, hasSize(1));
        FindFlavorsResult result = flavors.get(0);
        assertThat(result.getId(), equalTo(flavor.getId()));
        assertThat(result.getName(), equalTo(flavor.getName()));
        assertThat(result.getProjectName(), equalTo(project.getName()));
        assertThat(result.getVcpus(), equalTo(String.valueOf(flavor.getVcpus())));
        assertThat(result.getRam(), equalTo(String.valueOf(flavor.getRam())));
        assertThat(result.getRootDisk(), equalTo(String.valueOf(flavor.getRootDisk())));
        assertThat(result.getEphemeralDisk(), equalTo(String.valueOf(flavor.getEphemeralDisk())));
    }
    
    @Test
    public void testFindNetworks() {
        Project project = EntityGenerator.getProject();
        Network network = EntityGenerator.getNetwork();

        QueryResult queryResult = createQueryResult(
                Arrays.asList("networks.id", "networks.name", "projects.name", "networks.state", "networks.is_shared", "network_subnets.cidr"),
                Collections.singletonList(Arrays.asList(
                        network.getId(),
                        network.getName(),
                        project.getName(),
                        network.getState(),
                        String.valueOf(network.isIsShared()),
                        "5.255.255.0/24"
                ))
        );
        mockQueryRepository.setQueryResult(queryResult);
        FindNetworksRequest findNetworksRequest = requestProvider.get(FindNetworksRequest.class);
        List<FindNetworksResult> networks = projectsRepository.findNetworks(findNetworksRequest);
        assertThat(networks, hasSize(1));
        FindNetworksResult result = networks.get(0);
        assertThat(result.getId(), equalTo(network.getId()));
        assertThat(result.getName(), equalTo(network.getName()));
        assertThat(result.getProjectName(), equalTo(project.getName()));
        assertThat(result.getState(), equalTo(network.getState()));
        assertThat(result.isShared(), equalTo(network.isIsShared()));
        assertThat(result.getSubnets(), contains("5.255.255.0/24"));
    }
    
    @Test
    public void testFindKeypairs() {
        Project project = EntityGenerator.getProject();
        Keypair keypair = EntityGenerator.getKeypair();
        
        QueryResult queryResult = createQueryResult(
                Arrays.asList("keypairs.name", "keypairs.fingerprint", "projects.name"),
                Collections.singletonList(Arrays.asList(
                        keypair.getName(),
                        keypair.getFingerprint(),
                        project.getName()
                ))
        );
        mockQueryRepository.setQueryResult(queryResult);
        FindKeypairsRequest findKeypairsRequest = requestProvider.get(FindKeypairsRequest.class);
        List<FindKeypairsResult> keypairs = projectsRepository.findKeypairs(findKeypairsRequest);
        assertThat(keypairs, hasSize(1));
        FindKeypairsResult result = keypairs.get(0);
        assertThat(result.getName(), equalTo(keypair.getName()));
        assertThat(result.getFingerprint(), equalTo(keypair.getFingerprint()));
        assertThat(result.getProjectName(), equalTo(project.getName()));
    }

}