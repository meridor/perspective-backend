package org.meridor.perspective.openstack;

import org.openstack4j.model.compute.*;
import org.openstack4j.model.compute.ext.AvailabilityZone;
import org.openstack4j.model.network.Network;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Api {

    // Project operations

    Set<String> getComputeRegions();

    List<? extends Flavor> listFlavors();

    List<? extends Network> listNetworks();

    List<? extends AvailabilityZone> listAvailabilityZones();

    List<? extends Keypair> listKeypairs();

    AbsoluteLimit getQuota();

    //Instance operations

    String addInstance(ServerCreate serverConfig);

    void deleteInstance(String instanceId);

    void startInstance(String instanceId);

    void shutdownInstance(String instanceId);

    void rebootInstance(String instanceId);

    void hardRebootInstance(String instanceId);

    void resizeInstance(String instanceId, String flavorId);
    
    void confirmInstanceResize(String instanceId);
    
    void revertInstanceResize(String instanceId);

    void rebuildInstance(String instanceId, String imageId);

    void pauseInstance(String instanceId);
    
    void unpauseInstance(String instanceId);

    void suspendInstance(String instanceId);

    void resumeInstance(String instanceId);

    List<? extends Server> listInstances();

    Optional<Server> getInstanceById(String instanceId);

    String getInstanceConsoleUrl(String instanceId, String consoleType);

    // Image operations

    String addImage(String instanceId, String imageName);

    void deleteImage(String imageId);

    List<? extends org.openstack4j.model.image.Image> listImages();

    Optional<org.openstack4j.model.image.Image> getImageById(String imageId);

}
