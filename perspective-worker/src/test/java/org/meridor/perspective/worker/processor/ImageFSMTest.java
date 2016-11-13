package org.meridor.perspective.worker.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.ImageState;
import org.meridor.perspective.events.*;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.fsm.Yatomata;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.ImageState.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/fsm-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class ImageFSMTest {

    @Autowired
    private FSMBuilderAware fsmBuilderAware;

    @Autowired
    private ImagesAware imagesAware;

    @Test
    public void testOnImageQueued() {
        testOnImageSyncEvent(ImageQueuedEvent.class, QUEUED);
    }
    
    @Test
    public void testOnImageSaving() {
        testOnImageSyncEvent(ImageSavingEvent.class, SAVING);
    }
    
    @Test
    public void onImageAdding() {
        testOnImageEvent(ImageSavingEvent.class, SAVING, false);
    }
    
    @Test
    public void testOnImageSaved() {
        testOnImageSyncEvent(ImageSavedEvent.class, SAVED);
    }
    
    @Test
    public void testOnImageDeletingSync() {
        testOnImageSyncEvent(ImageDeletingEvent.class, DELETING);
    }

    @Test
    public void testOnImageDeleting() {
        Image image = EntityGenerator.getImage();
        imagesAware.saveImage(image);
        String imageId = image.getId();
        assertThat(imagesAware.imageExists(imageId), is(true));
        fireEvent(ImageDeletingEvent.class, image, false);
        assertThat(imagesAware.imageExists(imageId), is(false));
    }

    @Test
    public void testOnImageError() {
        testOnImageSyncEvent(ImageErrorEvent.class, ERROR);
    }
    
    private void testOnImageSyncEvent(Class<? extends ImageEvent> cls, ImageState correctState) {
        testOnImageEvent(cls, correctState, true);
    }
    
    private void testOnImageEvent(Class<? extends ImageEvent> cls, ImageState correctState, boolean isSyncEvent) {
        Image image = EntityGenerator.getImage();
        String imageId = image.getId();
        assertThat(imagesAware.imageExists(imageId), is(false));
        fireEvent(cls, image, isSyncEvent);
        assertThat(imagesAware.imageExists(imageId), is(true));
        assertThat(imagesAware.getImage(imageId).get().getState(), equalTo(correctState));
    }

    private void fireEvent(Class<? extends ImageEvent> cls, Image image, boolean isSyncEvent) {
        Yatomata<ImageFSM> fsm = getFSMBuilder().build();
        ImageEvent imageEvent = EventFactory.imageEvent(cls, image);
        imageEvent.setSync(isSyncEvent);
        fsm.fire(imageEvent);
    }
    
    private Yatomata.Builder<ImageFSM> getFSMBuilder() {
         return fsmBuilderAware.get(ImageFSM.class);
    }
    
}