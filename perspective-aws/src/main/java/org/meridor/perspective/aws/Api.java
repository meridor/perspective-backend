package org.meridor.perspective.aws;

import com.amazonaws.services.ec2.model.*;
import org.meridor.perspective.beans.Flavor;

import java.util.List;
import java.util.Set;

public interface Api {

    // Project operations

    List<Flavor> listFlavors();

    List<NetworkInterface> listNetworks();

    List<Subnet> listSubnets();

    List<AvailabilityZone> listAvailabilityZones();

    List<KeyPairInfo> listKeypairs();

    // Instance operations

    List<Instance> listInstances(Set<String> instanceIds);

    List<Instance> listInstances();
    
    boolean rebootInstance(String instanceId);

    boolean startInstance(String instanceId);

    boolean shutdownInstance(String instanceId);

    boolean deleteInstance(String instanceId);


    // Image operations

    String addImage(String instanceId, String imageName);

    List<Image> listImages(Set<String> imageIds);

    List<Image> listImages(); //TODO: think whether all project ids should be saved to one image 

    boolean deleteImage(String imageId);

    
    void close();
    
}
