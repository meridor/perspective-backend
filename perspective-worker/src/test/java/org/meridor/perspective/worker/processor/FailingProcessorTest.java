package org.meridor.perspective.worker.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.events.*;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.messaging.MessageUtils;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * We simulate cloud operation failure and check that respective processor
 * also throws an exception. This is important for cloud operation retries to
 * work as expected.
 */
@ContextConfiguration(locations = "/META-INF/spring/always-failing-processor-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class FailingProcessorTest {

    @Autowired
    private InstancesProcessor instancesProcessor;
    
    @Autowired
    private ImagesProcessor imagesProcessor;
    
    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Test(expected = RuntimeException.class)
    public void testFailingInstancesProcessor() {
        Instance instance = EntityGenerator.getInstance();
        instancesAware.saveInstance(instance);
        InstanceRebootingEvent instanceRebootingEvent = new InstanceRebootingEvent();
        instanceRebootingEvent.setInstance(instance);
        instancesProcessor.process(MessageUtils.message(CloudType.MOCK, instanceRebootingEvent));
    }
    
    @Test(expected = RuntimeException.class)
    public void testFailingImagesProcessor() {
        Image image = EntityGenerator.getImage();
        imagesAware.saveImage(image);
        ImageDeletingEvent imageDeletingEvent = new ImageDeletingEvent();
        imageDeletingEvent.setImage(image);
        imagesProcessor.process(MessageUtils.message(CloudType.MOCK, imageDeletingEvent));
    }
    
}