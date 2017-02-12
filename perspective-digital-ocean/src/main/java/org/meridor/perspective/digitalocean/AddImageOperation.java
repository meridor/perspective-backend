package org.meridor.perspective.digitalocean;

import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
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
import static org.meridor.perspective.digitalocean.ApiUtils.createFakeImageId;

@Component
public class AddImageOperation implements ProcessingOperation<Image, Image> {

    private static final Logger LOG = LoggerFactory.getLogger(AddImageOperation.class);

    private final ApiProvider apiProvider;

    private final IdGenerator idGenerator;

    private final ProjectsAware projectsAware;

    private final InstancesAware instancesAware;

    @Autowired
    public AddImageOperation(ApiProvider apiProvider, IdGenerator idGenerator, ProjectsAware projectsAware, InstancesAware instancesAware) {
        this.apiProvider = apiProvider;
        this.idGenerator = idGenerator;
        this.projectsAware = projectsAware;
        this.instancesAware = instancesAware;
    }

    @Override
    public Image perform(Cloud cloud, Supplier<Image> supplier) {
        Image image = supplier.get();
        String imageName = image.getName();
        try {
            if (image.getProjectIds().isEmpty()) {
                throw new IllegalStateException("Image should contain projectId");
            }
            String projectId = image.getProjectIds().get(0);
            Optional<Project> projectCandidate = projectsAware.getProject(projectId);
            if (!projectCandidate.isPresent()) {
                throw new IllegalArgumentException(String.format("Failed to add image: project with ID = %s does not exist", projectId));
            }
            String region = projectCandidate.get().getMetadata().get(MetadataKey.REGION);
            String instanceId = image.getInstanceId();
            Optional<Instance> instanceCandidate = instancesAware.getInstance(instanceId);
            if (!instanceCandidate.isPresent()) {
                throw new IllegalArgumentException(String.format("Failed to add image: instance with ID = %s does not exist", image.getInstanceId()));
            }
            Instance instance = instanceCandidate.get();
            Integer dropletId = Integer.valueOf(instance.getRealId());
            Api api = apiProvider.getApi(cloud);
            api.addImage(dropletId, imageName);
            image.getMetadata().put(MetadataKey.REGION, region);
            
            /*
                Unfortunately DigitalOcean API does not instantly return snapshot
                id. To fix this we save image with fake ID generated from name.
                This fake image is deleted during next images sync.
             */
            String imageId = createFakeImageId(idGenerator, cloud, imageName);
            image.setId(imageId);
            LOG.debug("Added image {} (fake id = {})", imageName, image.getId());
            return image;
        } catch (Exception e) {
            LOG.error("Failed to add image " + imageName, e);
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_IMAGE};
    }
}
