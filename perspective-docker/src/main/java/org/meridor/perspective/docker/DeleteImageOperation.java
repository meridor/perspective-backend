package org.meridor.perspective.docker;

import org.jclouds.docker.DockerApi;
import org.jclouds.docker.features.ImageApi;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.DELETE_IMAGE;

@Component
public class DeleteImageOperation implements ConsumingOperation<Image> {

    private static Logger LOG = LoggerFactory.getLogger(DeleteImageOperation.class);

    @Autowired
    private DockerApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Supplier<Image> supplier) {
        try (DockerApi dockerApi = apiProvider.getApi(cloud)) {
            ImageApi imageApi = dockerApi.getImageApi();
            Image image = supplier.get();
            String imageId = image.getMetadata().get(MetadataKey.ID);
            imageApi.deleteImage(imageId);
            LOG.debug("Deleted image {} ({})", image.getName(), image.getId());
            return true;
        } catch (IOException e) {
            LOG.error("Failed to delete image", e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{DELETE_IMAGE};
    }

}
