package org.meridor.perspective.docker;

import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
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

    private final ApiProvider apiProvider;

    private final IdGenerator idGenerator;

    private final InstancesAware instancesAware;

    @Autowired
    public AddImageOperation(ApiProvider apiProvider, IdGenerator idGenerator, InstancesAware instancesAware) {
        this.apiProvider = apiProvider;
        this.idGenerator = idGenerator;
        this.instancesAware = instancesAware;
    }

    @Override
    public Image perform(Cloud cloud, Supplier<Image> supplier) {
        Image image = supplier.get();
        try {
            Api api = apiProvider.getApi(cloud);
            String instanceId = image.getInstanceId();
            Optional<Instance> instanceCandidate = instancesAware.getInstance(instanceId);
            if (!instanceCandidate.isPresent()) {
                throw new IllegalArgumentException(String.format("Failed to add image: instance with ID = %s does not exist", image.getInstanceId()));
            }
            String instanceRealId = instanceCandidate.get().getRealId();
            String imageId = api.addImage(instanceRealId, image.getName());
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
