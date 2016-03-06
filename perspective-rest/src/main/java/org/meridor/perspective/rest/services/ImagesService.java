package org.meridor.perspective.rest.services;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.events.ImageDeletingEvent;
import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.events.ImageSavingEvent;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.meridor.perspective.beans.DestinationName.WRITE_TASKS;
import static org.meridor.perspective.beans.ImageState.DELETING;
import static org.meridor.perspective.beans.ImageState.QUEUED;
import static org.meridor.perspective.events.EventFactory.*;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Service
public class ImagesService {
    
    private static final Logger LOG = LoggerFactory.getLogger(ImagesService.class);
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Destination(WRITE_TASKS)
    private Producer producer;

    public Optional<Image> getImageById(String imageId) {
        LOG.info("Getting image with id = {}", imageId);
        return imagesAware.getImage(imageId);
    }

    public void addImages(List<Image> images) {
        for (Image image : images) {
            LOG.info("Queuing image {} for saving", image);
            String temporaryId = uuid();
            image.setId(temporaryId);
            image.setCreated(now());
            image.setTimestamp(now());
            image.setState(QUEUED);
            imagesAware.saveImage(image);
            ImageSavingEvent event = imageEvent(ImageSavingEvent.class, image);
            event.setTemporaryImageId(temporaryId);
            producer.produce(message(image.getCloudType(), event));
        }
    }
    
    public void deleteImages(List<String> imageIds) {
        for (String imageId : imageIds) {
            whenImageExists(
                    imageId,
                    i -> {
                        i.setState(DELETING);
                        return i;
                    },
                    i -> {
                        LOG.info("Queuing image {} for removal", imageId);
                        return imageEvent(ImageDeletingEvent.class, i);
                    }
            );
        }

    }
    
    private void whenImageExists(String imageId, Function<Image, Image> imageProcessor, Function<Image, ImageEvent> eventProvider) {
        Optional<Image> imageCandidate = imagesAware.getImage(imageId);
        if (imageCandidate.isPresent()) {
            Image image = imageCandidate.get();
            ImageEvent event = eventProvider.apply(image);
            Image updatedImage = imageProcessor.apply(image);
            imagesAware.saveImage(updatedImage);
            producer.produce(message(image.getCloudType(), event));
        } else {
            LOG.info("Image {} not found", imageId);
        }
    }

}
