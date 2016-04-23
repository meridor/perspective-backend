package org.meridor.perspective.worker.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.events.ImageSavedEvent;
import org.meridor.perspective.events.InstanceLaunchedEvent;
import org.meridor.perspective.framework.messaging.MessageUtils;
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
    
    @Test(expected = RuntimeException.class)
    public void testFailingInstancesProcessor() {
        instancesProcessor.process(MessageUtils.message(CloudType.MOCK, new InstanceLaunchedEvent()));
    }
    
    @Test(expected = RuntimeException.class)
    public void testFailingImagesProcessor() {
        imagesProcessor.process(MessageUtils.message(CloudType.MOCK, new ImageSavedEvent()));
    }
    
}