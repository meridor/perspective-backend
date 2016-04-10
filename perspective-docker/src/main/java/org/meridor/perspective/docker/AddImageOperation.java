package org.meridor.perspective.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_IMAGE;

@Component
public class AddImageOperation implements ProcessingOperation<Image, Image> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AddImageOperation.class);
    
    @Autowired
    private DockerApiProvider apiProvider;
    
    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private InstancesAware instancesAware;

    @Override
    public Image perform(Cloud cloud, Supplier<Image> supplier) {
        Image image = supplier.get();
        try {
            DockerClient dockerApi = apiProvider.getApi(cloud);
            String instanceId = image.getInstanceId();
            Optional<Instance> instanceCandidate = instancesAware.getInstance(instanceId);
            if (!instanceCandidate.isPresent()) {
                throw new IllegalArgumentException(String.format("Failed to add image: instance with ID = %s does not exist", image.getInstanceId()));
            }
            String instanceRealId = instanceCandidate.get().getRealId();
            ContainerConfig containerConfig = ContainerConfig.builder().build();
            ContainerCreation createdImage = dockerApi.commitContainer(
                    instanceRealId,
                    image.getName(),
                    null,
                    containerConfig,
                    null,
                    null
            );
            String imageId = createdImage.id();
            image.setRealId(imageId);
            String id = idGenerator.getImageId(cloud, imageId);
            image.setId(id);
            LOG.debug("Added image {} ({})", image.getName(), image.getId());
            return image;
        } catch (Exception e) {
            LOG.error("Failed to add image " + image.getName(), e);
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_IMAGE};
    }
}
