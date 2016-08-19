package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.sql.impl.storage.impl.DerivedTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public abstract class ImagesBasedTableFetcher<T> extends DerivedTableFetcher<Image, T> {
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Override
    protected Function<Predicate<Image>, Collection<Image>> getPredicateFetcher() {
        return predicate -> imagesAware.getImages(predicate);
    }

    @Override
    protected Collection<Image> getAllBaseEntities() {
        return imagesAware.getImages();
    }
}
