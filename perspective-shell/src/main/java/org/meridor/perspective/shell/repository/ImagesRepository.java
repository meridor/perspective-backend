package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.query.AddImagesQuery;
import org.meridor.perspective.shell.query.DeleteImagesQuery;
import org.meridor.perspective.shell.query.ShowImagesQuery;

import java.util.List;
import java.util.Set;

public interface ImagesRepository {
    
    List<Image> showImages(ShowImagesQuery showImagesQuery);

    Set<String> addImages(AddImagesQuery addImagesQuery);

    Set<String> deleteImages(DeleteImagesQuery deleteImagesQuery);
    
}
