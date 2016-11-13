package org.meridor.perspective.digitalocean;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.backend.storage.ProjectsAware;
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
    private ApiProvider apiProvider;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private ProjectsAware projectsAware;

    @Autowired
    private InstancesAware instancesAware;

    @Override
    public Image perform(Cloud cloud, Supplier<Image> supplier) {
        Image image = supplier.get();
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
            String realId = String.valueOf(api.addImage(dropletId, image.getName()));
            image.getMetadata().put(MetadataKey.REGION, region);
            image.setRealId(realId);
            String imageId = idGenerator.getImageId(cloud, realId);
            image.setId(imageId);
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
