package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class ImagesTableFetcher extends BaseTableFetcher<Image> {

    @Autowired
    private ImagesAware imagesAware;

    @Override
    protected Class<Image> getBeanClass() {
        return Image.class;
    }

    @Override
    public String getTableName() {
        return TableName.IMAGES.getTableName();
    }

    @Override
    protected Collection<Image> getRawEntities(Set<String> ids) {
        return imagesAware.getImages(ids);
    }

    @Override
    protected Collection<Image> getAllRawEntities() {
        return imagesAware.getImages();
    }
}
