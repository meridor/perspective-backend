package org.meridor.perspective.openstack;

import com.google.common.base.Optional;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.Network;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.AvailabilityZoneDetails;
import org.jclouds.openstack.nova.v2_0.extensions.AvailabilityZoneApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.meridor.perspective.beans.AvailabilityZone;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.CloudType.OPENSTACK;
import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
@Operation(cloud = OPENSTACK, type = LIST_PROJECTS)
public class ListProjectsOperation {
    
    private static Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);
    
    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @EntryPoint
    public void listProjects(Cloud cloud, Consumer<Set<Project>> consumer) {
        
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud); NeutronApi neutronApi = apiProvider.getNeutronApi(cloud)) {
            Set<Project> projects = new HashSet<>();
            for (String region : novaApi.getConfiguredRegions()) {
                Project project = createProject(cloud, region);
                
                FlavorApi flavorApi = novaApi.getFlavorApi(region);
                addFlavors(project, flavorApi);

                NetworkApi networkApi = neutronApi.getNetworkApi(region);
                addNetworks(project, networkApi);
                
//                Optional<AvailabilityZoneApi> availabilityZoneApi = novaApi.getAvailabilityZoneApi(region);
//                if (availabilityZoneApi.isPresent()) {
//                    addAvailabilityZones(availabilityZoneApi.get(), project);
//                }
                
                projects.add(project);
            }
            LOG.debug("Fetched {} projects from Openstack API", projects.size());
            consumer.accept(projects);
        } catch (IOException e) {
            LOG.error("Failed to fetch projects", e);
            consumer.accept(Collections.emptySet());
        }
        
    }

    private Project createProject(Cloud cloud, String region) {
        String projectId = String.format("%s - %s", cloud.getName(), region);
        Project project = new Project();
        project.setId(projectId);
        project.setName(projectId);
        project.setTimestamp(ZonedDateTime.now());
        return project;
    }
    
    private void addFlavors(Project project, FlavorApi flavorApi) {
        for (Flavor flavor : flavorApi.listInDetail().concat()) {
            org.meridor.perspective.beans.Flavor flavorToAdd = new org.meridor.perspective.beans.Flavor();
            flavorToAdd.setId(flavor.getId());
            flavorToAdd.setName(flavor.getName());
            flavorToAdd.setVcpus(flavor.getVcpus());
            flavorToAdd.setRam(flavor.getRam());
            flavorToAdd.setRootDisk(flavor.getDisk());
            Optional<Integer> ephemeralDisk = flavor.getEphemeral();
            flavorToAdd.setEphemeralDisk(ephemeralDisk.isPresent() ? ephemeralDisk.get() : 0);
            flavorToAdd.setHasSwap(flavor.getSwap().isPresent());
            flavorToAdd.setIsPublic(true);
            project.getFlavors().add(flavorToAdd);
        }
    }
    
    private void addNetworks(Project project, NetworkApi networkApi) {
        for (Network network : networkApi.list().concat()) {
            org.meridor.perspective.beans.Network networkToAdd = new org.meridor.perspective.beans.Network();
            networkToAdd.setId(network.getId());
            networkToAdd.setName(network.getName());
            networkToAdd.setIsShared(network.getShared());
            networkToAdd.setState(network.getStatus().name());
            for (String subnet : network.getSubnets()) {
                networkToAdd.getSubnets().add(subnet);
            }
            project.getNetworks().add(networkToAdd);
        }
    }
    
    private void addAvailabilityZones(AvailabilityZoneApi availabilityZoneApi, Project project) {
        for (AvailabilityZoneDetails az : availabilityZoneApi.listInDetail()) {
            AvailabilityZone availabilityZone = new AvailabilityZone();
            availabilityZone.setName(az.getName());
            project.getAvailabilityZones().add(availabilityZone);
        }
    }
    
}
