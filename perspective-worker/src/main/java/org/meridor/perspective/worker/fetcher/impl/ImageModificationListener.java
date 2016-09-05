package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.delayToLimit;

@Component
public class ImageModificationListener extends LastModificationListener<Image> {
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Value("${perspective.fetch.delay.images}")
    private int imagesFetchDelay;
    
    @PostConstruct
    public void init() {
        imagesAware.addImageListener(this);
    }

    @Override
    protected int getLongTimeAgoLimit() {
        return delayToLimit(imagesFetchDelay);
    }

    @Override
    protected String getId(Image image) {
        return image.getId();
    }

    @Override
    protected String getCloudId(Image image) {
        return image.getCloudId();
    }

    @Override
    protected Instant getLastModifiedInstant(Image image) {
        return image.getTimestamp().toInstant();
    }
}
