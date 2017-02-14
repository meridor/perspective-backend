package org.meridor.perspective.googlecloud;

import com.google.api.client.util.Lists;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.*;
import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.config.Cloud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.READ;
import static org.meridor.perspective.googlecloud.IdUtils.stringToImageId;
import static org.meridor.perspective.googlecloud.IdUtils.stringToInstanceId;

@Component
public class ApiProviderImpl implements ApiProvider {

    @Override
    public Api getApi(Cloud cloud) {
        return new ApiImpl(cloud);
    }

    @Override
    public void forEachRegion(Cloud cloud, BiConsumer<Region, Api> action) throws Exception {
        Api api = getApi(cloud);
        api.listRegions().forEach(r -> action.accept(r, api));
    }

    private static class ApiImpl implements Api {

        private static final Logger LOG = LoggerFactory.getLogger(Api.class);


        private final Compute computeApi;

        ApiImpl(Cloud cloud) {
            Credentials credentials = getCredentials(cloud);
            this.computeApi = createComputeApi(credentials);
        }

        private Credentials getCredentials(Cloud cloud) {

            Path jsonPath = Paths.get(cloud.getCredential());
            try (InputStream inputStream = Files.newInputStream(jsonPath, READ)) {
                return GoogleCredentials.fromStream(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(String.format(
                        "Failed to read JSON credentials file [%s]",
                        jsonPath.toAbsolutePath().toString()
                ), e);
            }
        }

        private Compute createComputeApi(Credentials credentials) {
            return ComputeOptions.newBuilder()
                    .setCredentials(credentials)
                    .build().getService();
        }

        private static boolean executeInstanceOperation(String instanceId, Function<InstanceId, Operation> action) {
            return isOperationSuccessful(action.apply(stringToInstanceId(instanceId)));
        }

        private static boolean executeImageOperation(String imageId, Function<ImageId, Operation> action) {
            return isOperationSuccessful(action.apply(stringToImageId(imageId)));
        }

        private static boolean isOperationSuccessful(Operation operation) {
            try {
                Operation completedOperation = operation.waitFor();
                if (completedOperation != null) {
                    if (completedOperation.getErrors() != null) {
                        completedOperation.getErrors()
                                .forEach(
                                        e -> LOG.error(
                                                "Error {} in {}: {}",
                                                e.getCode(),
                                                e.getLocation(),
                                                e.getMessage()
                                        )
                                );
                    } else {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public List<MachineType> listFlavors() {
            return Lists.newArrayList(computeApi.listMachineTypes().getValues());
        }

        @Override
        public List<Network> listNetworks() {
            return Lists.newArrayList(computeApi.listNetworks().getValues());
        }

        @Override
        public List<Region> listRegions() {
            return Lists.newArrayList(computeApi.listRegions().getValues());
        }

        @Override
        public List<Keypair> listKeypairs() {
            //TODO: see https://cloud.google.com/compute/docs/instances/adding-removing-ssh-keys#project-wide 
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteInstance(String instanceId) {
            return executeInstanceOperation(instanceId, id -> computeApi.deleteInstance(id));
        }

        @Override
        public boolean startInstance(String instanceId) {
            return executeInstanceOperation(instanceId, id -> computeApi.start(id));
        }

        @Override
        public boolean shutdownInstance(String instanceId) {
            return executeInstanceOperation(instanceId, id -> computeApi.stop(id));
        }

        @Override
        public boolean rebootInstance(String instanceId) {
            //There's no soft reboot operation in Google Cloud
            return shutdownInstance(instanceId) && startInstance(instanceId);
        }

        @Override
        public boolean hardRebootInstance(String instanceId) {
            return executeInstanceOperation(instanceId, id -> computeApi.reset(id));
        }

        @Override
        public boolean resizeInstance(String instanceId, String flavorId) {
            return executeInstanceOperation(instanceId, id -> {
                MachineTypeId machineTypeId = IdUtils.stringToMachineTypeId(flavorId);
                return computeApi.setMachineType(id, machineTypeId);
            });
        }

        @Override
        public List<com.google.cloud.compute.Instance> listInstances() {
            return Lists.newArrayList(computeApi.listInstances().getValues());
        }

        @Override
        public boolean deleteImage(String imageId) {
            return executeImageOperation(imageId, id -> computeApi.deleteImage(id));
        }

        @Override
        public List<com.google.cloud.compute.Image> listImages() {
            return Lists.newArrayList(computeApi.listImages().getValues());
        }

        @Override
        public List<Snapshot> listSnapshots() {
            return Lists.newArrayList(computeApi.listSnapshots().getValues());
        }

    }
}
