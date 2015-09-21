package org.meridor.perspective.openstack;

import com.google.common.collect.FluentIterable;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
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
    private OpenstackApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Image>> consumer) {
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            Set<Image> images = new HashSet<>();
            for (String region : novaApi.getConfiguredRegions()) {
                try {
                    ImageApi imageApi = novaApi.getImageApi(region);
                    FluentIterable<org.jclouds.openstack.nova.v2_0.domain.Image> imagesList = imageApi.listInDetail().concat();
                    imagesList.forEach(img -> images.add(createImage(img)));
                    LOG.debug("Fetched images for cloud = {}, region = {}", cloud.getName(), region);
                } catch (Exception e) {
                    LOG.error("Failed to fetch images for cloud = {}, region = {}", cloud.getName(), region);
                }
            }

            LOG.info("Fetched {} images overall for cloud = {}", images.size(), cloud.getName());
            consumer.accept(images);
            return true;
        } catch (IOException e) {
            LOG.error("Failed to fetch images for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_IMAGES};
    }

    private Image createImage(org.jclouds.openstack.nova.v2_0.domain.Image openstackImage) {
        Image image = new Image();
        String imageId = idGenerator.generate(Image.class, openstackImage.getId());
        image.setId(imageId);
        image.setName(openstackImage.getName());
        image.setState(stateFromStatus(openstackImage.getStatus()));
        ZonedDateTime created = ZonedDateTime.ofInstant(
                openstackImage.getCreated().toInstant(),
                ZoneId.systemDefault()
        );
        image.setCreated(created);
        image.setState(stateFromStatus(openstackImage.getStatus()));
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                openstackImage.getUpdated().toInstant(),
                ZoneId.systemDefault()
        );
        image.setTimestamp(timestamp);
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.ID, openstackImage.getId());
        image.setMetadata(metadata);
        return image;
    }

    private static ImageState stateFromStatus(org.jclouds.openstack.nova.v2_0.domain.Image.Status status) {
        switch (status) {
            case SAVING:
                return ImageState.SAVING;
            case DELETED:
                return ImageState.DELETING;
            case UNRECOGNIZED:
            case UNKNOWN:
            case ERROR:
                return ImageState.ERROR;
            default:
            case ACTIVE:
                return ImageState.SAVED;
        }
    }

}
