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
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_IMAGES;

@Component
public class ListImagesOperation implements SupplyingOperation<Set<Image>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListImagesOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @Autowired
    private ImagesAware imagesAware;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Image>> consumer) {
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            Integer overallImagesCount = 0;
            for (String region : novaApi.getConfiguredRegions()) {
                Set<Image> images = new HashSet<>();
                try {
                    ImageApi imageApi = novaApi.getImageApi(region);
                    FluentIterable<org.jclouds.openstack.nova.v2_0.domain.Image> imagesList = imageApi.listInDetail().concat();
                    imagesList.forEach(img -> images.add(createOrModifyImage(img, cloud, region)));
                    Integer regionImagesCount = images.size();
                    overallImagesCount += regionImagesCount;
                    LOG.debug("Fetched {} images for cloud = {}, region = {}", regionImagesCount, cloud.getName(), region);
                    consumer.accept(images);
                } catch (Exception e) {
                    LOG.error(String.format("Failed to fetch images for cloud = %s, region = %s", cloud.getName(), region), e);
                }
            }

            LOG.info("Fetched {} images overall for cloud = {}", overallImagesCount, cloud.getName());
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

    private Image createOrModifyImage(org.jclouds.openstack.nova.v2_0.domain.Image openstackImage, Cloud cloud, String region) {
        String imageId = idGenerator.getImageId(cloud, openstackImage.getId());
        String projectId = idGenerator.getProjectId(cloud, region);
        Optional<Image> imageCandidate = imagesAware.getImage(imageId);
        if (!imageCandidate.isPresent()) {
            Image image = createImage(openstackImage, imageId, projectId);
            return updateImage(image, openstackImage, region, projectId);
        } else {
            return updateImage(imageCandidate.get(), openstackImage, region, projectId);
        }
    }
    
    private Image createImage(org.jclouds.openstack.nova.v2_0.domain.Image openstackImage, String imageId, String projectId) {
        Image image = new Image();
        image.setId(imageId);
        image.setRealId(openstackImage.getId());
        List<String> projectIds = Collections.singletonList(projectId);
        image.setProjectIds(projectIds);
        return image;
    } 
    
    private Image updateImage(Image image, org.jclouds.openstack.nova.v2_0.domain.Image openstackImage, String region, String projectId) {
        HashSet<String> projectIds = new HashSet<>(image.getProjectIds());
        projectIds.add(projectId);
        image.setProjectIds(new ArrayList<>(projectIds));
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
        metadata.put(MetadataKey.REGION, region);
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
