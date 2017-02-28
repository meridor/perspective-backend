package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.*;
import org.meridor.perspective.beans.Keypair;

import java.util.List;
import java.util.Optional;

public interface Api {

    // Project operations

    List<MachineType> listFlavors();

    List<Network> listNetworks();

    List<Subnetwork> listSubnetworks();

    List<Region> listRegions();

    List<Keypair> listKeypairs();

    // Instance operations

    boolean deleteInstance(String instanceId);

    boolean startInstance(String instanceId);

    boolean shutdownInstance(String instanceId);

    boolean rebootInstance(String instanceId);

    boolean hardRebootInstance(String instanceId);

    boolean resizeInstance(String instanceId, String flavorId);

    List<com.google.cloud.compute.Instance> listInstances();

    List<Disk> listDisks();

    Optional<Instance> getInstanceById(String instanceId);

    // Image operations

    boolean deleteImage(String imageId);

    List<com.google.cloud.compute.Image> listImages();

    List<com.google.cloud.compute.Snapshot> listSnapshots();
}
