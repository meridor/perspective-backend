package org.meridor.perspective.framework.storage;

import org.meridor.perspective.beans.Image;

import java.util.Collection;
import java.util.Optional;

public interface ImagesAware {


    boolean imageExists(String imageId);

    Collection<Image> getImages(Optional<String> query) throws IllegalQueryException;

    Optional<Image> getImage(String imageId);

    void saveImage(Image image);

    boolean isImageDeleted(String imageId);

    void deleteImage(Image image);

}
