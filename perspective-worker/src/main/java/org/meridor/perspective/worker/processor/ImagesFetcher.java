package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.IfNotLocked;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.meridor.perspective.beans.DestinationName.TASKS;
import static org.meridor.perspective.events.EventFactory.imageToEvent;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Component
public class ImagesFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ImagesFetcher.class);

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @Destination(TASKS)
    private Producer producer;

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private WorkerMetadata workerMetadata;

    @Scheduled(fixedDelayString = "${perspective.fetch.delay.images}")
    public void fetchImages() {
        cloudConfigurationProvider.getClouds().forEach(this::fetchCloudImages);
    }
    
    @IfNotLocked
    protected void fetchCloudImages(Cloud cloud) {
        LOG.info("Fetching images list for cloud {}", cloud.getName());
        Set<Image> images = new HashSet<>();
        try {
            if (!operationProcessor.<Set<Image>>consume(cloud, OperationType.LIST_IMAGES, images::addAll)) {
                throw new RuntimeException("Failed to get images list from the cloud");
            }
            CloudType cloudType = workerMetadata.getCloudType();
            for (Image image : images) {
                image.setCloudType(cloudType);
                image.setCloudId(cloud.getId());
                ImageEvent event = imageToEvent(image);
                event.setSync(true);
                producer.produce(message(cloudType, event));
            }
            LOG.debug("Saved images state for cloud {} to queue", cloud.getName());
        } catch (Exception e) {
            LOG.error("Error while fetching images list for cloud " + cloud.getName(), e);
        }
    }

}
