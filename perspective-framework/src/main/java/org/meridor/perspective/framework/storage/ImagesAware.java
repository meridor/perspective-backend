package org.meridor.perspective.framework.storage;

import org.meridor.perspective.beans.Image;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface ImagesAware {


    boolean imageExists(String imageId);

    Collection<Image> getImages();
    
    Collection<Image> getImages(Set<String> ids);
    
    Collection<Image> getImages(Predicate<Image> predicate);

    Optional<Image> getImage(String imageId);

    void saveImage(Image image);

    boolean isImageDeleted(String imageId);

    void deleteImage(String imageId);

    void addImageListener(EntityListener<Image> listener);
}
