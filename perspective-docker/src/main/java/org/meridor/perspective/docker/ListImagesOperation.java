package org.meridor.perspective.docker;

import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.ImageSummary;
import org.jclouds.docker.features.ImageApi;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.MetadataMap;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_IMAGES;

@Component
public class ListImagesOperation implements SupplyingOperation<Set<Image>> {

    private static Logger LOG = LoggerFactory.getLogger(ListImagesOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private DockerApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Image>> consumer) {
        try (DockerApi dockerApi = apiProvider.getApi(cloud)) {
            Set<Image> instances = new HashSet<>();
            ImageApi imageApi = dockerApi.getImageApi();
            for (ImageSummary imageSummary : imageApi.listImages()) {
                org.jclouds.docker.domain.Image image = imageApi.inspectImage(imageSummary.id());
                instances.add(createImage(image));
            }
            LOG.debug("Fetched {} images", instances.size());
            consumer.accept(instances);
            return true;
        } catch (IOException e) {
            LOG.error("Failed to fetch images", e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_IMAGES};
    }

    private Image createImage(org.jclouds.docker.domain.Image dockerImage) {
        Image image = new Image();
        String imageId = idGenerator.generate(Image.class, dockerImage.id());
        image.setId(imageId);
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.AUTHOR, dockerImage.author());
        metadata.put(MetadataKey.ARCHITECTURE, dockerImage.architecture());
        metadata.put(MetadataKey.ID, dockerImage.id());
        metadata.put(MetadataKey.OPERATING_SYSTEM, dockerImage.os());
        metadata.put(MetadataKey.PARENT, dockerImage.parent());
        image.setMetadata(metadata);
        image.setName(dockerImage.comment());
        ZonedDateTime created = ZonedDateTime.ofInstant(
                dockerImage.created().toInstant(),
                ZoneId.systemDefault()
        );
        image.setCreated(created);
        image.setState(ImageState.SAVED);
        image.setTimestamp(created);
        return image;
    }

}
