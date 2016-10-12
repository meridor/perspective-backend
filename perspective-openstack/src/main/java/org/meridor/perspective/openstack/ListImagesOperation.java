package org.meridor.perspective.openstack;

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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_IMAGES;

@Component
public class ListImagesOperation implements SupplyingOperation<Set<Image>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListImagesOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private ApiProvider apiProvider;
    
    @Autowired
    private ImagesAware imagesAware;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Image>> consumer) {
        try {
            final AtomicInteger overallImagesCount = new AtomicInteger();
            apiProvider.forEachComputeRegion(cloud, (region, api) -> {
                Set<Image> images = new HashSet<>();
                try {
                    List<? extends org.openstack4j.model.image.Image> imagesList = api.listImages();
                    imagesList.forEach(img -> images.add(createOrModifyImage(img, cloud, region)));
                    Integer regionImagesCount = images.size();
                    overallImagesCount.getAndUpdate(c -> c + regionImagesCount);
                    LOG.debug("Fetched {} images for cloud = {}, region = {}", regionImagesCount, cloud.getName(), region);
                    consumer.accept(images);
                } catch (Exception e) {
                    LOG.error(String.format("Failed to fetch images for cloud = %s, region = %s", cloud.getName(), region), e);
                }
            });

            LOG.info("Fetched {} images overall for cloud = {}", overallImagesCount, cloud.getName());
            return true;
        } catch (Exception e) {
            LOG.error("Failed to fetch images for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Set<Image>> consumer) {
        try {
            Map<String, Set<String>> fetchMap = getFetchMap(ids);
            apiProvider.forEachComputeRegion(cloud, (region, api) -> {
                if (fetchMap.containsKey(region)) {
                    fetchMap.get(region).forEach(realId -> {
                        Optional<org.openstack4j.model.image.Image> imageCandidate = api.getImageById(realId);
                        if (imageCandidate.isPresent()) {
                            org.openstack4j.model.image.Image image = imageCandidate.get();
                            LOG.debug("Fetched image {} ({}) for cloud = {}, region = {}", image.getName(), image.getId(), cloud.getName(), region);
                            consumer.accept(Collections.singleton(
                                    createOrModifyImage(image, cloud, region)
                            ));
                        }
                    });
                }
            });
            return true;
        } catch (Exception e) {
            LOG.error(String.format(
                    "Failed to fetch images with ids = %s for cloud = %s",
                    ids,
                    cloud.getName()
            ), e);
            return false;
        }
    }

    private Map<String, Set<String>> getFetchMap(Set<String> ids) {
        Map<String, Set<String>> ret = new HashMap<>();
        ids.forEach(id -> {
            Optional<Image> imageCandidate = imagesAware.getImage(id);
            if (imageCandidate.isPresent()) {
                Image image = imageCandidate.get();
                String region = image.getMetadata().get(MetadataKey.REGION);
                ret.putIfAbsent(region, new HashSet<>());
                ret.get(region).add(image.getRealId());
            }
        });
        return ret;
    }
    
    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_IMAGES};
    }

    private Image createOrModifyImage(org.openstack4j.model.image.Image openstackImage, Cloud cloud, String region) {
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
    
    private Image createImage(org.openstack4j.model.image.Image openstackImage, String imageId, String projectId) {
        Image image = new Image();
        image.setId(imageId);
        image.setRealId(openstackImage.getId());
        List<String> projectIds = Collections.singletonList(projectId);
        image.setProjectIds(projectIds);
        return image;
    } 
    
    private Image updateImage(Image image, org.openstack4j.model.image.Image openstackImage, String region, String projectId) {
        HashSet<String> projectIds = new HashSet<>(image.getProjectIds());
        projectIds.add(projectId);
        image.setProjectIds(new ArrayList<>(projectIds));
        image.setName(openstackImage.getName());
        image.setState(stateFromStatus(openstackImage.getStatus()));
        ZonedDateTime created = ZonedDateTime.ofInstant(
                openstackImage.getCreatedAt().toInstant(),
                ZoneId.systemDefault()
        );
        image.setCreated(created);
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                openstackImage.getUpdatedAt().toInstant(),
                ZoneId.systemDefault()
        );
        image.setTimestamp(timestamp);
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, region);
        image.setMetadata(metadata);
        return image;
    }
    
    private static ImageState stateFromStatus(org.openstack4j.model.image.Image.Status status) {
        switch (status) {
            case SAVING:
                return ImageState.SAVING;
            case QUEUED:
            case PENDING_DELETE:
            case DELETED:
                return ImageState.DELETING;
            case UNRECOGNIZED:
            case KILLED:
                return ImageState.ERROR;
            default:
            case ACTIVE:
                return ImageState.SAVED;
        }
    }

}
