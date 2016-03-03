package org.meridor.perspective.shell.repository;

import org.meridor.perspective.shell.request.AddImagesRequest;
import org.meridor.perspective.shell.request.FindImagesRequest;
import org.meridor.perspective.shell.result.FindImagesResult;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ImagesRepository {
    
    List<FindImagesResult> findImages(FindImagesRequest findImagesRequest);

    Set<String> addImages(AddImagesRequest addImagesRequest);

    Set<String> deleteImages(Collection<String> imageIds);
    
}
