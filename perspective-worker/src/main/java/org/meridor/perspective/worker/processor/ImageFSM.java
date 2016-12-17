package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

import java.util.Optional;

@Component
@FSM(start = ImageNotAvailableEvent.class)
@Transitions({
        //Image sync
        @Transit(from = ImageNotAvailableEvent.class, on = ImageQueuedEvent.class, to = ImageQueuedEvent.class),
        @Transit(from = ImageNotAvailableEvent.class, on = ImageSavingEvent.class, to = ImageSavingEvent.class),
        @Transit(from = ImageNotAvailableEvent.class, on = ImageSavedEvent.class, to = ImageSavedEvent.class),
        @Transit(from = ImageNotAvailableEvent.class, on = ImageErrorEvent.class, to = ImageErrorEvent.class),
        @Transit(from = ImageNotAvailableEvent.class, on = ImageDeletingEvent.class, to = ImageDeletingEvent.class),
        @Transit(from = ImageQueuedEvent.class, on = ImageQueuedEvent.class, to = ImageQueuedEvent.class),
        @Transit(from = ImageSavingEvent.class, on = ImageSavingEvent.class, to = ImageSavingEvent.class),
        @Transit(from = ImageSavedEvent.class, on = ImageSavedEvent.class, to = ImageSavedEvent.class),
        @Transit(from = ImageErrorEvent.class, on = ImageErrorEvent.class, to = ImageErrorEvent.class),
        @Transit(from = ImageDeletingEvent.class, on = ImageDeletingEvent.class, to = ImageDeletingEvent.class),

        //Image save
        @Transit(from = ImageQueuedEvent.class, on = ImageSavingEvent.class, to = ImageSavingEvent.class),
        @Transit(from = ImageSavingEvent.class, on = ImageSavedEvent.class, to = ImageSavedEvent.class),
        @Transit(from = ImageSavingEvent.class, on = ImageErrorEvent.class, to = ImageErrorEvent.class),

        //Image removal
        @Transit(from = ImageSavedEvent.class, on = ImageDeletingEvent.class, stop = true),
        @Transit(from = ImageErrorEvent.class, on = ImageDeletingEvent.class, stop = true)
})
public class ImageFSM {

    private static final Logger LOG = LoggerFactory.getLogger(ImageFSM.class);

    private final OperationProcessor operationProcessor;

    private final CloudConfigurationProvider cloudConfigurationProvider;

    private final ImagesAware imagesAware;

    @Autowired
    public ImageFSM(OperationProcessor operationProcessor, ImagesAware imagesAware, CloudConfigurationProvider cloudConfigurationProvider) {
        this.operationProcessor = operationProcessor;
        this.imagesAware = imagesAware;
        this.cloudConfigurationProvider = cloudConfigurationProvider;
    }

    @BeforeTransit
    public void beforeTransit(ImageEvent imageEvent) {
        LOG.trace("Doing transition for event {}", imageEvent);
    }

    @OnTransit
    public void onImageQueued(ImageQueuedEvent event) {
        if (event.isSync()) {
            Image image = event.getImage();
            LOG.info("Marking image {} ({}) as queued", image.getName(), image.getId());
            image.setState(ImageState.QUEUED);
            imagesAware.saveImage(image);
        }
    }

    @OnTransit
    public void onImageSaving(ImageSavingEvent event) {
        Image image = event.getImage();
        String cloudId = image.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (event.isSync()) {
            LOG.info("Marking image {} ({}) as saving", image.getName(), image.getId());
            image.setState(ImageState.SAVING);
            imagesAware.saveImage(image);
        } else {
            LOG.info("Adding image {} ({})", image.getName(), image.getId());
            Optional<Image> updatedImageCandidate = operationProcessor.process(cloud, OperationType.ADD_IMAGE, () -> image);
            if (!updatedImageCandidate.isPresent()) {
                throw new RuntimeException(String.format("Failed to add %s", image));
            }
            Image updatedImage = updatedImageCandidate.get();
            updatedImage.setState(ImageState.SAVING);
            
            //Swap images with random UUID and real ID
            String temporaryImageId = event.getTemporaryImageId();
            if (temporaryImageId != null && imagesAware.imageExists(temporaryImageId)) {
                imagesAware.deleteImage(temporaryImageId);
            }
            imagesAware.saveImage(updatedImage);
        }
        
    }

    @OnTransit
    public void onImageSaved(ImageSavedEvent event) {
        if (event.isSync()) {
            Image image = event.getImage();
            LOG.info("Marking image {} ({}) as saved", image.getName(), image.getId());
            image.setState(ImageState.SAVED);
            imagesAware.saveImage(image);
        }
    }
    
    @OnTransit
    public void onImageDeleting(ImageDeletingEvent event) {
        Image image = event.getImage();
        String cloudId = image.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (event.isSync()) {
            LOG.info("Marking image {} ({}) as deleting", image.getName(), image.getId());
            image.setState(ImageState.DELETING);
            imagesAware.saveImage(image);
        } else if (imagesAware.imageExists(image.getId())) {
            if (operationProcessor.supply(cloud, OperationType.DELETE_IMAGE, () -> image)) {
                LOG.info("Deleting image {} ({})", image.getName(), image.getId());
                imagesAware.deleteImage(image.getId());
            } else {
                throw new RuntimeException(String.format("Failed to delete %s", image));
            }
        } else {
            LOG.error("Can't delete image {} ({}) - not exists", image.getName(), image.getId());
        }
    }

    @OnTransit
    public void onImageError(ImageErrorEvent event) {
        Image image = event.getImage();
        LOG.info("Changing image {} ({}) status to error with reason = {}", image.getName(), image.getId());
        image.setState(ImageState.ERROR);
        imagesAware.saveImage(image);
    }

    @OnTransit
    public void onUnknownEvent(ImageEvent event) {
        LOG.warn("Skipping unknown event {}", event);
    }

    @OnException
    public void onUnsupportedOperationException(UnsupportedOperationException e) {
        LOG.error("Trying to do an unsupported operation", e);
    }

}
