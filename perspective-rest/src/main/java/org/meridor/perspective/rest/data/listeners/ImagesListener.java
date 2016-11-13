package org.meridor.perspective.rest.data.listeners;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.backend.storage.StorageEvent;
import org.meridor.perspective.rest.data.converters.ImageConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.meridor.perspective.rest.data.TableName.*;

@Component
public class ImagesListener extends BaseEntityListener<Image> {

    @Autowired
    private ImagesAware imagesAware;
    
    @PostConstruct
    public void init() {
        imagesAware.addImageListener(this);
    }
    
    @Override
    public void onEvent(Image image, Image oldImage, StorageEvent event) {
        updateEntity(event, IMAGES.getTableName(), image, oldImage);
        updateDerivedEntities(event, IMAGE_METADATA.getTableName(), image, oldImage, ImageConverters::imageToMetadata);
        updateDerivedEntities(event, PROJECT_IMAGES.getTableName(), image, oldImage, ImageConverters::imageToProjectImages);
    }

}
