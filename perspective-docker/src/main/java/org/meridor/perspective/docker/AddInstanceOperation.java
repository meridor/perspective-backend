package org.meridor.perspective.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;

@Component
public class AddInstanceOperation implements ProcessingOperation<Instance, Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AddInstanceOperation.class);

    @Autowired
    private DockerApiProvider apiProvider;

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        try {
            DockerClient dockerApi = apiProvider.getApi(cloud);
            String command = instance.getMetadata().get(MetadataKey.COMMAND);
            String imageId = instance.getImage().getMetadata().get(MetadataKey.ID);
            ContainerConfig containerConfig = ContainerConfig.builder()
                    .cmd(command)
                    .image(imageId)
                    .build();
            ContainerCreation createdContainer = dockerApi.createContainer(containerConfig, instance.getName());
            String instanceId = createdContainer.id();
            instance.getMetadata().put(MetadataKey.ID, instanceId);
            LOG.debug("Added instance {} ({})", instance.getName(), instance.getId());
            return instance;
        } catch (Exception e) {
            LOG.error("Failed to add instance " + instance.getName(), e);
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_INSTANCE};
    }
}
