package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;

import java.util.Collections;
import java.util.Optional;

import static org.meridor.perspective.events.EventFactory.imageToEvent;

@Component
public class ImagesProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ImagesProcessor.class);

    @Autowired
    private ImagesAware storage;

    @Autowired
    private FSMBuilderAware fsmBuilderAware;

    @Override
    public void process(Message message) {
        LOG.trace("Processing message {}", message.getId());
        Optional<ImageEvent> imageEvent = message.getPayload(ImageEvent.class);
        if (imageEvent.isPresent()) {
            processImages(imageEvent.get());
        } else {
            LOG.error("Skipping empty message {}", message.getId());
        }
    }

    private void processImages(ImageEvent event) {
        Image imageFromEvent = event.getImage();
        Optional<Image> imageOrEmpty = storage.getImage(imageFromEvent.getId());
        if (imageOrEmpty.isPresent()) {
            Image image = imageOrEmpty.get();
            ImageEvent currentState = imageToEvent(image);
            Yatomata<ImageFSM> fsm = fsmBuilderAware.get(ImageFSM.class).build(currentState);
            LOG.debug(
                    "Updating image {} ({}) from state = {} to state = {}",
                    image.getName(),
                    image.getId(),
                    currentState.getClass().getSimpleName(),
                    event.getClass().getSimpleName()
            );
            fsm.fire(event);
        } else if (event.isSync() && !storage.isImageDeleted(imageFromEvent.getId())) {
            LOG.debug(
                    "Syncing image {} ({}) with state = {} for the first time",
                    event.getImage().getName(),
                    event.getImage().getId(),
                    event.getClass().getSimpleName()
            );
            Yatomata<ImageFSM> fsm = fsmBuilderAware.get(ImageFSM.class).build();
            fsm.fire(event);
        } else {
            LOG.debug(
                    "Will not update image {} ({}) as it does not exist or was already deleted",
                    imageFromEvent.getName(),
                    imageFromEvent.getId()
            );
        }
    }

    @Override
    public boolean isPayloadSupported(Class<?> payloadClass) {
        return ImageEvent.class.isAssignableFrom(payloadClass);
    }
}
