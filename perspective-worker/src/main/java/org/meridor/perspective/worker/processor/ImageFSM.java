package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.meridor.perspective.worker.processor.event.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

import java.util.Optional;

import static org.meridor.perspective.events.EventFactory.imageEventToState;

@Component
@FSM(start = ImageNotAvailableEvent.class)
@Transitions({

        //Image queued
        @Transit(from = {ImageNotAvailableEvent.class, ImageQueuedEvent.class}, on = ImageQueuedEvent.class, to = ImageQueuedEvent.class),

        //Image saving
        @Transit(from = {ImageNotAvailableEvent.class, ImageQueuedEvent.class, ImageSavingEvent.class}, on = ImageSavingEvent.class, to = ImageSavingEvent.class),

        //Image saved
        @Transit(from = {ImageNotAvailableEvent.class, ImageSavingEvent.class, ImageSavedEvent.class}, on = ImageSavedEvent.class, to = ImageSavedEvent.class),

        //Image error
        @Transit(from = ImageEvent.class, on = ImageErrorEvent.class, to = ImageErrorEvent.class),
        
        //Image removal
        @Transit(from = {ImageNotAvailableEvent.class, ImageErrorEvent.class, ImageSavedEvent.class, ImageDeletingEvent.class}, on = ImageDeletingEvent.class, stop = true),
        
})
public class ImageFSM {

    private static final Logger LOG = LoggerFactory.getLogger(ImageFSM.class);

    private final OperationProcessor operationProcessor;

    private final CloudConfigurationProvider cloudConfigurationProvider;

    private final ImagesAware imagesAware;

    private final MailSender mailSender;
    
    @Autowired
    public ImageFSM(OperationProcessor operationProcessor, ImagesAware imagesAware, CloudConfigurationProvider cloudConfigurationProvider, MailSender mailSender) {
        this.operationProcessor = operationProcessor;
        this.imagesAware = imagesAware;
        this.cloudConfigurationProvider = cloudConfigurationProvider;
        this.mailSender = mailSender;
    }

    @BeforeTransit
    public void beforeTransit(@Event ImageEvent imageEvent) {
        LOG.trace("Doing transition for event {}", imageEvent);
    }

    @OnTransit
    public void onImageQueued(@Event ImageQueuedEvent event) {
        if (event.isSync()) {
            Image image = event.getImage();
            LOG.info("Marking image {} ({}) as queued", image.getName(), image.getId());
            image.setState(ImageState.QUEUED);
            imagesAware.saveImage(image);
        }
    }

    @OnTransit
    public void onImageSaving(@Event ImageSavingEvent event) {
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
    public void onImageSaved(@Event ImageSavedEvent event) {
        if (event.isSync()) {
            Image image = event.getImage();
            LOG.info("Marking image {} ({}) as saved", image.getName(), image.getId());
            image.setState(ImageState.SAVED);
            imagesAware.saveImage(image);
        }
    }

    @OnTransit
    public void onImageDeleting(@Event ImageDeletingEvent event) {
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
    public void onImageError(@FromState ImageEvent from, @Event ImageErrorEvent event) {
        Image image = event.getImage();
        LOG.info("Changing image {} ({}) status to error", image.getName(), image.getId());
        image.setState(ImageState.ERROR);
        imagesAware.saveImage(image);
        if (!(from instanceof ImageErrorEvent) && !(from instanceof ImageNotAvailableEvent)) {
            LOG.info("Sending letter about image {} ({}) error", image.getName(), image.getId());
            String message = getMailMessage(imageEventToState(from), image);
            mailSender.sendLetter(message);
        }
    }

    private String getMailMessage(ImageState previousImageState, Image image) {
        switch (previousImageState) {
            case DELETING:
                return String.format("Failed to delete image %s (%s)", image.getName(), image.getId());
            case QUEUED:
            case SAVING:
            case SAVED:
                return String.format("Failed to save image %s (%s)", image.getName(), image.getId());
        }
        throw new IllegalArgumentException(String.format(
                "Unsupported image state: %s. This is a bug.",
                previousImageState.value()
        ));
    }

    @OnException
    public void onUnsupportedOperationException(UnsupportedOperationException e) {
        LOG.error("Trying to do an unsupported operation", e);
    }

}
