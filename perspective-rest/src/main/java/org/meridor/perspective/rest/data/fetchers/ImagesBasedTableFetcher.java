package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.sql.impl.storage.impl.DerivedTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public abstract class ImagesBasedTableFetcher<T> extends DerivedTableFetcher<Image, T> {
    
    @Autowired
    private ImagesAware imagesAware;

    @Override
    protected Collection<Image> getBaseEntities(Set<String> ids) {
        return imagesAware.getImages(ids);
    }

    @Override
    protected Collection<Image> getAllBaseEntities() {
        return imagesAware.getImages();
    }
}
