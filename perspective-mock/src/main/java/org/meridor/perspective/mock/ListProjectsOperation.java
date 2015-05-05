package org.meridor.perspective.mock;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
@Operation(cloud = MOCK, type = LIST_PROJECTS)
public class ListProjectsOperation {
    
    @EntryPoint
    public void listProjects(Projects projects) {
        Project project = new Project();
        project.setName("test-project");
        project.setId(getRandomId());
        Region region = getRegion();
        project.getRegions().add(region);
        projects.getProjects().add(project);
    }
    
    private Region getRegion() {
        Region region = new Region();
        region.setId(getRandomId());
        region.setName("test-region");
        region.getFlavors().add(getFlavor());
        region.getAvailabilityZones().add(getAvailabilityZone());
        region.getNetworks().add(getNetwork());
        return region;
    }
    
    private Flavor getFlavor() {
        Flavor flavor = new Flavor();
        flavor.setId(getRandomId());
        flavor.setName("test-flavor");
        flavor.setVcpus(2);
        flavor.setRam(2048);
        return flavor;
    }
    
    private AvailabilityZone getAvailabilityZone() {
        AvailabilityZone availabilityZone = new AvailabilityZone();
        availabilityZone.setName("test-zone");
        return availabilityZone;
    }
    
    private Network getNetwork() {
        Network network = new Network();
        network.setId(getRandomId());
        network.setName("test-network");
        network.getSubnets().add("5.255.210.0/24");
        return network;
    }
    
    private String getRandomId() {
        return UUID.randomUUID().toString();
    }
    
}
