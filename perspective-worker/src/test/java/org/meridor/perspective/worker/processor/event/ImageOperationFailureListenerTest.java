package org.meridor.perspective.worker.processor.event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.backend.messaging.Message;
import org.meridor.perspective.events.ImageDeletingEvent;
import org.meridor.perspective.events.ImageErrorEvent;
import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.events.ImageSavingEvent;

import java.util.Arrays;
import java.util.Collection;

import static org.meridor.perspective.backend.EntityGenerator.getImage;
import static org.meridor.perspective.backend.messaging.MessageUtils.message;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.events.EventFactory.imageEvent;

@RunWith(Parameterized.class)
public class ImageOperationFailureListenerTest extends BaseOperationFailureTest {

    @Parameterized.Parameters(name = "Failing \"{0}\" should be processed")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {ImageSavingEvent.class},
                {ImageDeletingEvent.class},
                {ImageErrorEvent.class},
        });
    }

    private final Class<? extends ImageEvent> cls;

    public ImageOperationFailureListenerTest(Class<? extends ImageEvent> cls) {
        this.cls = cls;
    }
    
    @Test
    public void testListenerWorks() throws Exception {
        ImageEvent imageEvent = imageEvent(cls, getImage());
        Message message = message(MOCK, imageEvent);
        assertListenerWorks(message);
    }
    

}