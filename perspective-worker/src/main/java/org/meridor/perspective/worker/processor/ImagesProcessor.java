package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.messaging.Processor;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;

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
        CloudType cloudType = imageFromEvent.getCloudType();
        Optional<Image> imageOrEmpty = storage.getImage(imageFromEvent.getId());
        if (imageOrEmpty.isPresent()) {
            Image image = imageOrEmpty.get();
            ImageEvent currentState = imageToEvent(image);
            Yatomata<ImageFSM> fsm = fsmBuilderAware.get(ImageFSM.class).build(currentState);
            event.setImage(image);
            LOG.debug(
                    "Updating image {} from cloud {} from state = {} to state = {}",
                    image.getId(),
                    cloudType,
                    currentState.getClass().getSimpleName(),
                    event.getClass().getSimpleName()
            );
            fsm.fire(event);
        } else if (event.isSync() && !storage.isImageDeleted(imageFromEvent.getId())) {
            LOG.debug(
                    "Syncing image {} from cloud {} with state = {} for the first time",
                    event.getImage().getId(),
                    cloudType,
                    event.getClass().getSimpleName()
            );
            Yatomata<ImageFSM> fsm = fsmBuilderAware.get(ImageFSM.class).build();
            fsm.fire(event);
        } else {
            LOG.debug(
                    "Will not update image {} from cloud = {} as it does not exist or was already deleted",
                    imageFromEvent.getId(),
                    cloudType
            );
        }
    }
}
