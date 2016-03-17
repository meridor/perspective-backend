package org.meridor.perspective.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ImageInfo;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_IMAGES;

@Component
public class ListImagesOperation implements SupplyingOperation<Set<Image>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListImagesOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private DockerApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Image>> consumer) {
        try {
            DockerClient dockerApi = apiProvider.getApi(cloud);
            Set<Image> instances = new HashSet<>();
            List<com.spotify.docker.client.messages.Image> dockerImages = dockerApi.listImages(DockerClient.ListImagesParam.allImages(false));
            for (com.spotify.docker.client.messages.Image image : dockerImages) {
                ImageInfo imageInfo = dockerApi.inspectImage(image.id());
                instances.add(createImage(image, imageInfo, cloud));
            }
            LOG.debug("Fetched {} images", instances.size());
            consumer.accept(instances);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to fetch images", e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_IMAGES};
    }

    private Image createImage(com.spotify.docker.client.messages.Image dockerImage, ImageInfo dockerImageInfo, Cloud cloud) {
        Image image = new Image();
        String imageId = idGenerator.getImageId(cloud, dockerImageInfo.id());
        String projectId = idGenerator.getProjectId(cloud);
        image.setId(imageId);
        image.setRealId(dockerImageInfo.id());
        image.setProjectIds(Collections.singletonList(projectId));
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.AUTHOR, dockerImageInfo.author());
        metadata.put(MetadataKey.ARCHITECTURE, dockerImageInfo.architecture());
        metadata.put(MetadataKey.OPERATING_SYSTEM, dockerImageInfo.os());
        metadata.put(MetadataKey.PARENT, dockerImageInfo.parent());
        metadata.put(MetadataKey.SIZE, dockerImage.virtualSize().toString());
        image.setMetadata(metadata);
        String imageName = dockerImage.repoTags().isEmpty() ?
                String.format(
                        "docker-%s-%s-%s",
                        dockerImageInfo.os(),
                        dockerImageInfo.architecture(),
                        dockerImageInfo.id().substring(0, Math.min(dockerImageInfo.size().intValue(), 8))
                ) :
                dockerImage.repoTags().get(0);
        image.setName(imageName);
        ZonedDateTime created = ZonedDateTime.ofInstant(
                dockerImageInfo.created().toInstant(),
                ZoneId.systemDefault()
        );
        image.setCreated(created);
        image.setState(ImageState.SAVED);
        image.setTimestamp(created);
        return image;
    }

}
