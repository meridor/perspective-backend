package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.IfNotLocked;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.worker.fetcher.LastModificationAware;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.beans.DestinationName.READ_TASKS;
import static org.meridor.perspective.events.EventFactory.imageToEvent;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Component
public class ImagesFetcher extends BaseFetcher<Image> {

    private static final Logger LOG = LoggerFactory.getLogger(ImagesFetcher.class);

    @Destination(READ_TASKS)
    private Producer producer;

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private WorkerMetadata workerMetadata;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Value("${perspective.fetch.delay.images}")
    private int fullSyncDelay;

    
    @IfNotLocked(lockName = "all")
    @Override
    public void fetch(Cloud cloud) {
        LOG.info("Fetching images list for cloud = {}", cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_IMAGES, getConsumer(cloud))) {
                throw new RuntimeException("Failed to get images list from cloud = " + cloud.getName());
            }
        } catch (Exception e) {
            LOG.error("Error while fetching images list for cloud = " + cloud.getName(), e);
        }
    }
    
    @IfNotLocked(lockName = "ids")
    @Override
    public void fetch(Cloud cloud, Set<String> ids) {
        LOG.info("Fetching images with ids = {} for cloud = {}", ids, cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_IMAGES, ids, getConsumer(cloud))) {
                throw new RuntimeException(String.format(
                        "Failed to get images with ids = %s from cloud = %s",
                        ids,
                        cloud.getName()
                ));
            }
        } catch (Exception e) {
            LOG.error(String.format(
                    "Error while fetching images with ids = %s for cloud = %s",
                    ids,
                    cloud.getName()
            ), e);
        }
    }

    @Override
    protected int getFullSyncDelay() {
        return fullSyncDelay;
    }

    @Override
    protected LastModificationAware<Image> getLastModificationAware() {
        return applicationContext.getBean(ImageModificationListener.class);
    }

    private Consumer<Set<Image>> getConsumer(Cloud cloud) {
        return images -> {
            CloudType cloudType = workerMetadata.getCloudType();
            for (Image image : images) {
                image.setCloudType(cloudType);
                image.setCloudId(cloud.getId());
                ImageEvent event = imageToEvent(image);
                event.setSync(true);
                producer.produce(message(cloudType, event));
            }
            LOG.debug("Saved {} fetched images for cloud = {} to queue", images.size(), cloud.getName());
        };
    }

}
