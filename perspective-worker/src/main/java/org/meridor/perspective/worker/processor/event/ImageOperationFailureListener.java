package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.events.ImageDeletingEvent;
import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.events.ImageSavingEvent;
import org.springframework.stereotype.Component;

@Component
public class ImageOperationFailureListener extends OperationFailureListener<ImageEvent> {
    
    @Override
    protected void processEvent(ImageEvent imageEvent) {
        Image image = imageEvent.getImage();
        if (imageEvent instanceof ImageSavingEvent) {
            sendImageLetter("Failed to save image %s (%s)", image);
        } else if (imageEvent instanceof ImageDeletingEvent) {
            sendImageLetter("Failed to delete image %s (%s)", image);
        } else {
            sendLetter(String.format(
                    "Failed to process %s event for image %s (%s)",
                    imageEvent.getClass().getSimpleName(),
                    image.getName(),
                    image.getId()
            ));
        }
    }

    @Override
    protected Class<ImageEvent> getEventClass() {
        return ImageEvent.class;
    }

    private void sendImageLetter(String text, Image image) {
        sendLetter(String.format(text, image.getName(), image.getId()));
    }

}
