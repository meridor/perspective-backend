package org.meridor.perspective.framework;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.CloudType;

import java.util.Collections;

import static org.meridor.perspective.events.EventFactory.now;

public class EntityGenerator {

    public static Project getProject() {
        Project project = new Project();
        project.setName("test-project - test-region");
        project.setId("test-project");
        project.setCloudId(CloudType.MOCK.value());
        project.setCloudType(CloudType.MOCK);
        project.getFlavors().add(getFlavor());
        project.getNetworks().add(getNetwork());
        project.getKeypairs().add(getKeypair());
        project.getAvailabilityZones().add(getAvailabilityZone());
        MetadataMap metadataMap = new MetadataMap();
        metadataMap.put(MetadataKey.REGION, "test-region");
        project.setMetadata(metadataMap);
        return project;
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
        network.getSubnets().add(new Subnet(){
            {
                setId("test-subnet");
                setName("test-subnet");
                setCidr(new Cidr() {
                    {
                        setAddress("5.255.210.0");
                        setPrefixSize(24);
                    }
                });
            }
        });
        return network;
    }
    
    public static Keypair getKeypair() {
        Keypair keypair = new Keypair();
        keypair.setName("test-keypair");
        keypair.setFingerprint("key-fingerprint");
        keypair.setPublicKey("test-public-key");
        return keypair;
    }

    public static Image getImage() {
        Image image = new Image();
        image.setId("test-image");
        image.setRealId("test-image");
        image.setProjectIds(Collections.singletonList(getProject().getId()));
        image.setName("test-image");
        image.setState(ImageState.SAVED);
        image.setCreated(now().minusDays(2));
        image.setTimestamp(now().minusHours(4));
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.INSTANCE_ID, "test-instance");
        image.setMetadata(metadata);
        return image;
    }

    public static Instance getInstance() {
        Instance instance = new Instance();
        instance.setId("test-instance");
        instance.setTimestamp(now());
        instance.setCreated(now());
        instance.setCloudType(CloudType.MOCK);
        instance.setProjectId("test-project");
        instance.setName("test-instance");
        instance.setFlavor(getFlavor());
        instance.getNetworks().add(getNetwork());
        instance.setImage(getImage());
        instance.setState(InstanceState.LAUNCHED);
        instance.setAvailabilityZone(getAvailabilityZone());
        MetadataMap metadataMap = new MetadataMap();
        metadataMap.put(MetadataKey.REGION, "test-region");
        instance.setMetadata(metadataMap);
        return instance;
    }

    public static Instance getErrorInstance() {
        Instance instance = getInstance();
        instance.setCreated(now().minusDays(5));
        instance.setTimestamp(now().minusDays(3));
        instance.setId("error-instance");
        instance.setName("error-instance");
        instance.setState(InstanceState.ERROR);
        return instance;
    }

}
