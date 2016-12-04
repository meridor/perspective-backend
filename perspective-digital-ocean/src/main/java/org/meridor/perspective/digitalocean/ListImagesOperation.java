package org.meridor.perspective.digitalocean;

import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.ImageState;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.LIST_IMAGES;

@Component
public class ListImagesOperation implements SupplyingOperation<Set<Image>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListImagesOperation.class);

    private final IdGenerator idGenerator;

    private final ApiProvider apiProvider;

    private final ImagesAware imagesAware;

    @Autowired
    public ListImagesOperation(ImagesAware imagesAware, IdGenerator idGenerator, ApiProvider apiProvider) {
        this.imagesAware = imagesAware;
        this.idGenerator = idGenerator;
        this.apiProvider = apiProvider;
    }

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Image>> consumer) {
        try {
            final AtomicInteger overallImagesCount = new AtomicInteger();
            Api api = apiProvider.getApi(cloud);
            Set<Image> images = new HashSet<>();
            try {
                List<com.myjeeva.digitalocean.pojo.Image> imagesList = api.listImages();
                imagesList.forEach(img -> images.add(processImage(img, cloud)));
                Integer regionImagesCount = images.size();
                overallImagesCount.getAndUpdate(c -> c + regionImagesCount);
                LOG.debug("Fetched {} images for cloud = {}", regionImagesCount, cloud.getName());
                consumer.accept(images);
            } catch (Exception e) {
                LOG.error(String.format("Failed to fetch images for cloud = %s", cloud.getName()), e);
            }
            return true;
        } catch (Exception e) {
            LOG.error("Failed to fetch images for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Set<Image>> consumer) {
        try {
            Set<String> realIds = getRealIds(ids);
            Api api = apiProvider.getApi(cloud);
            for (String realId : realIds) {
                Optional<com.myjeeva.digitalocean.pojo.Image> imageCandidate = api.getImageById(Integer.valueOf(realId));
                if (imageCandidate.isPresent()) {
                    com.myjeeva.digitalocean.pojo.Image image = imageCandidate.get();
                    LOG.debug("Fetched image {} ({}) for cloud = {}", image.getName(), image.getId(), cloud.getName());
                    consumer.accept(Collections.singleton(
                            processImage(image, cloud)
                    ));
                }
            }
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

    private Set<String> getRealIds(Set<String> ids) {
        Set<String> realIds = new HashSet<>();
        ids.forEach(id -> {
            Optional<Image> imageCandidate = imagesAware.getImage(id);
            if (imageCandidate.isPresent()) {
                Image image = imageCandidate.get();
                String realId = image.getRealId();
                if (realId != null) {
                    realIds.add(realId);
                }
            }
        });
        return realIds;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_IMAGES};
    }

    private Image processImage(com.myjeeva.digitalocean.pojo.Image digitalOceanImage, Cloud cloud) {
        String imageId = idGenerator.getImageId(cloud, String.valueOf(digitalOceanImage.getId()));
        List<String> projectIds = digitalOceanImage.getRegions().stream()
                .map(r -> idGenerator.getProjectId(cloud, r))
                .collect(Collectors.toList());
        return createImage(digitalOceanImage, imageId, projectIds);
    }

    private Image createImage(com.myjeeva.digitalocean.pojo.Image digitalOceanImage, String imageId, List<String> projectIds) {
        Image image = new Image();
        image.setId(imageId);
        image.setRealId(String.valueOf(digitalOceanImage.getId()));
        String imageName =
                digitalOceanImage.isSnapshot() || digitalOceanImage.isBackup() ?
                        digitalOceanImage.getName() :
                        String.format("%s %s", digitalOceanImage.getDistribution(), digitalOceanImage.getName());
        image.setName(imageName);
        image.setProjectIds(projectIds);
        image.setState(ImageState.SAVED);
        ZonedDateTime created = ZonedDateTime.ofInstant(
                digitalOceanImage.getCreatedDate().toInstant(),
                ZoneId.systemDefault()
        );
        image.setCreated(created);
        image.setTimestamp(created);
        MetadataMap metadata = new MetadataMap();
        image.setMetadata(metadata);
        return image;
    }

}
