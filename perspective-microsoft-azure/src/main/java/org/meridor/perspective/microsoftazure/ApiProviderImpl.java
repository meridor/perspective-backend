package org.meridor.perspective.microsoftazure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.SshPublicKey;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.OperationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Component
public class ApiProviderImpl implements ApiProvider {

    private final OperationUtils operationUtils;

    @Autowired
    public ApiProviderImpl(OperationUtils operationUtils) {
        this.operationUtils = operationUtils;
    }

    @Override
    public Api getApi(Cloud cloud) {
        return new ApiImpl(cloud);
    }

    @Override
    public void forEachRegion(Cloud cloud, BiConsumer<String, Api> action) throws Exception {
        Arrays.stream(Region.values()).forEach(r -> action.accept(r.name(), getApi(cloud)));
    }

    private class ApiImpl implements Api {

        private final Azure client;

        ApiImpl(Cloud cloud) {
            try {
                AzureTokenCredentials credentials = operationUtils.fromProjectAndUserName(
                        cloud,
                        (projectName, userName) -> new ApplicationTokenCredentials(
                                userName,
                                projectName,
                                cloud.getCredential(),
                                AzureEnvironment.AZURE //This could be parameterized in properties file! 
                        )
                );
                this.client = Azure.configure()
                        .withLogLevel(LogLevel.HEADERS)
                        .authenticate(credentials)
                        .withDefaultSubscription();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<VirtualMachineSize> listFlavors() {
            return null;
        }

        @Override
        public List<Network> listNetworks() {
            return client.networks().list();
        }

        @Override
        public List<SshPublicKey> listKeypairs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<VirtualMachine> listVirtualMachines() {
            return client.virtualMachines().list();
        }

        @Override
        public Optional<VirtualMachine> getVirtualMachineById(String virtualMachineId) {
            return Optional.ofNullable(client.virtualMachines().getById(virtualMachineId));
        }

        @Override
        public boolean rebootVirtualMachine(String virtualMachineId) {
            return false;
        }

        @Override
        public boolean startVirtualMachine(String virtualMachineId) {
            return false;
        }

        @Override
        public boolean shutdownVirtualMachine(String virtualMachineId) {
            return false;
        }

        @Override
        public boolean deleteVirtualMachine(String virtualMachineId) {
            return false;
        }

        @Override
        public String addImage(String virtualMachineId, String imageName) {
            return null;
        }

        @Override
        public List<VirtualMachineImage> listImages() {
            return null;
        }

        @Override
        public Optional<VirtualMachineImage> getImageById(String virtualMachineId) {
            return null;
        }

        @Override
        public boolean deleteImage(String imageId) {
            return false;
        }
    }
}
