package org.meridor.perspective.mock;

import org.meridor.perspective.beans.*;

public class EntityGenerator {

    public static Project getProject() {
        Project project = new Project();
        project.setName("test-project");
        project.setId("test-project");
        Region region = getRegion();
        project.getRegions().add(region);
        return project;
    }

    public static Region getRegion() {
        Region region = new Region();
        region.setId("test-region");
        region.setName("test-region");
        region.getFlavors().add(getFlavor());
        region.getAvailabilityZones().add(getAvailabilityZone());
        region.getNetworks().add(getNetwork());
        return region;
    }

    public static Flavor getFlavor() {
        Flavor flavor = new Flavor();
        flavor.setId("test-flavor");
        flavor.setName("test-flavor");
        flavor.setVcpus(2);
        flavor.setRam(2048);
        return flavor;
    }

    public static AvailabilityZone getAvailabilityZone() {
        AvailabilityZone availabilityZone = new AvailabilityZone();
        availabilityZone.setName("test-zone");
        return availabilityZone;
    }

    public static Network getNetwork() {
        Network network = new Network();
        network.setId("test-network");
        network.setName("test-network");
        network.getSubnets().add("5.255.210.0/24");
        return network;
    }
    
    public static Image getImage() {
        Image image = new Image();
        image.setId("test-instance");
        image.setName("test-image");
        image.setIsProtected(true);
        return image;
    }
    
    public static Instance getInstance() {
        Instance instance = new Instance();
        instance.setId("test-instance");
        instance.setProjectId("test-project");
        instance.setRegionId("test-region");
        instance.setName("test-instance");
        instance.setFlavor(getFlavor());
        instance.getNetworks().add(getNetwork());
        instance.setImage(getImage());
        instance.setStatus(InstanceStatus.LAUNCHED);
        return instance;
    }

}
