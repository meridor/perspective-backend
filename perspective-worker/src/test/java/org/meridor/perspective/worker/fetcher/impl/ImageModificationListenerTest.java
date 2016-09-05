package org.meridor.perspective.worker.fetcher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.worker.misc.impl.MockCloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/mocked-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ImageModificationListenerTest {

    @Autowired
    private ImagesAware imagesAware;
    
    @Autowired
    private ImageModificationListener imageModificationListener;
    
    private static final String CLOUD_ID = new MockCloud().getId();
    
    @Test
    public void testListen() {
        final String ID = "new-id";
        Image image = EntityGenerator.getImage();
        image.setId(ID);
        image.setCloudId(CLOUD_ID);
        image.setTimestamp(ZonedDateTime.now().minus(1, ChronoUnit.DAYS));
        imagesAware.saveImage(image);
        Set<String> ids = imageModificationListener.getIds(CLOUD_ID, LastModified.LONG_AGO);
        assertThat(ids, contains(ID));
    }
    
}