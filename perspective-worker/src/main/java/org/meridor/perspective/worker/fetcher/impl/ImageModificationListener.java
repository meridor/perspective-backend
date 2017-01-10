package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.worker.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.delayToLimit;

@Component
public class ImageModificationListener extends LastModificationListener<Image> {

    private final ImagesAware imagesAware;

    private final Config config;
    
    @Autowired
    public ImageModificationListener(ImagesAware imagesAware, Config config) {
        this.imagesAware = imagesAware;
        this.config = config;
    }

    @PostConstruct
    public void init() {
        showInfo();
        imagesAware.addImageListener(this);
    }

    @Override
    protected int getLongTimeAgoLimit() {
        return delayToLimit(config.getImagesFetchDelay());
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
