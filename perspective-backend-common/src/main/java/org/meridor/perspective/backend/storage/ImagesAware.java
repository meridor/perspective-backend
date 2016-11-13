package org.meridor.perspective.backend.storage;

import org.meridor.perspective.beans.Image;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ImagesAware {


    boolean imageExists(String imageId);

    Collection<Image> getImages();
    
    Collection<Image> getImages(Set<String> ids);

    Optional<Image> getImage(String imageId);

    void saveImage(Image image);

    boolean isImageDeleted(String imageId);

    void deleteImage(String imageId);

    void addImageListener(EntityListener<Image> listener);
}
