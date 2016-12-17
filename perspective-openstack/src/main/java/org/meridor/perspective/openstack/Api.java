package org.meridor.perspective.openstack;

import org.openstack4j.model.compute.*;
import org.openstack4j.model.compute.ext.AvailabilityZone;
import org.openstack4j.model.network.Network;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Api {

    // Project operations

    Set<String> listComputeRegions();

    List<? extends Flavor> listFlavors();

    List<? extends Network> listNetworks();

    List<? extends AvailabilityZone> listAvailabilityZones();

    String addKeypair(Keypair keypair);
    
    List<? extends Keypair> listKeypairs();

    AbsoluteLimit getQuota();

    //Instance operations

    String addInstance(ServerCreate serverConfig);

    boolean deleteInstance(String instanceId);

    boolean startInstance(String instanceId);

    boolean shutdownInstance(String instanceId);

    boolean rebootInstance(String instanceId);

    boolean hardRebootInstance(String instanceId);

    boolean resizeInstance(String instanceId, String flavorId);
    
    boolean confirmInstanceResize(String instanceId);
    
    boolean revertInstanceResize(String instanceId);

    boolean renameInstance(String instanceId, String newName);
    
    boolean rebuildInstance(String instanceId, String imageId);

    boolean pauseInstance(String instanceId);
    
    boolean unpauseInstance(String instanceId);
    
    boolean suspendInstance(String instanceId);

    boolean resumeInstance(String instanceId);

    List<? extends Server> listInstances();

    Optional<Server> getInstanceById(String instanceId);

    String getInstanceConsoleUrl(String instanceId, String consoleType);

    // Image operations

    String addImage(String instanceId, String imageName);

    void deleteImage(String imageId);

    List<? extends org.openstack4j.model.image.Image> listImages();

    Optional<org.openstack4j.model.image.Image> getImageById(String imageId);

}
