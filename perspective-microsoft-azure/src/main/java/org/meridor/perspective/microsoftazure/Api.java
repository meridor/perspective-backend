package org.meridor.perspective.microsoftazure;

import com.microsoft.azure.management.compute.SshPublicKey;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.network.Network;

import java.util.List;
import java.util.Optional;

public interface Api {

    // Project operations

    List<VirtualMachineSize> listFlavors();

    List<Network> listNetworks();

    List<SshPublicKey> listKeypairs();

    // Instance operations

    List<VirtualMachine> listVirtualMachines();

    Optional<VirtualMachine> getVirtualMachineById(String virtualMachineId);

    boolean rebootVirtualMachine(String virtualMachineId);

    boolean startVirtualMachine(String virtualMachineId);

    boolean shutdownVirtualMachine(String virtualMachineId);

    boolean deleteVirtualMachine(String virtualMachineId);


    // Image operations

    String addImage(String virtualMachineId, String imageName);

    List<VirtualMachineImage> listImages();

    Optional<VirtualMachineImage> getImageById(String virtualMachineId);

    boolean deleteImage(String imageId);

}
