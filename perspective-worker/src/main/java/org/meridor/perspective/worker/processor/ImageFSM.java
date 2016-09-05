package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.framework.storage.ImagesAware;
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

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @Autowired
    private ImagesAware storage;

    @BeforeTransit
    public void beforeTransit(ImageEvent imageEvent) {
        LOG.trace("Doing transition for event {}", imageEvent);
    }

    @OnTransit
    public void onImageQueued(ImageQueuedEvent event) {
        if (event.isSync()) {
            Image instance = event.getImage();
            LOG.info("Marking instance {} ({}) as queued", instance.getName(), instance.getId());
            instance.setState(ImageState.QUEUED);
            storage.saveImage(instance);
        }
    }

    @OnTransit
    public void onImageSaving(ImageSavingEvent event) throws Exception {
        Image image = event.getImage();
        String cloudId = image.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (event.isSync()) {
            LOG.info("Marking image {} ({}) as saving", image.getName(), image.getId());
            image.setState(ImageState.SAVING);
            storage.saveImage(image);
        } else {
            LOG.info("Adding image {} ({})", image.getName(), image.getId());
            Optional<Image> updatedImageCandidate = operationProcessor.process(cloud, OperationType.ADD_IMAGE, () -> image);
            if (!updatedImageCandidate.isPresent()) {
                throw new RuntimeException(String.format("Failed to add %s", image));
            }
            Image updatedImage = updatedImageCandidate.get();
            updatedImage.setState(ImageState.SAVING);
            
            //Swap images with random UUID and real ID
            storage.deleteImage(event.getTemporaryImageId());
            storage.saveImage(updatedImage);
        }
        
    }

    @OnTransit
    public void onImageSaved(ImageSavedEvent event) {
        if (event.isSync()) {
            Image image = event.getImage();
            LOG.info("Marking image {} ({}) as saved", image.getName(), image.getId());
            image.setState(ImageState.SAVED);
            storage.saveImage(image);
        }
    }
    
    @OnTransit
    public void onImageDeleting(ImageDeletingEvent event) throws Exception {
        Image image = event.getImage();
        String cloudId = image.getCloudId();
        Cloud cloud = cloudConfigurationProvider.getCloud(cloudId);
        if (storage.imageExists(image.getId())) {
            LOG.info("Deleting image {} ({})", image.getName(), image.getId());
            if (event.isSync() || operationProcessor.supply(cloud, OperationType.DELETE_IMAGE, () -> image)) {
                storage.deleteImage(image.getId());
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
        LOG.info("Changing image {} ({}) status to error with reason = {}", image.getName(), image.getId(), event.getErrorReason());
        image.setState(ImageState.ERROR);
        image.setErrorReason(event.getErrorReason());
        storage.saveImage(image);
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
