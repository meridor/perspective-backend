package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.rest.data.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ImagesTableFetcher extends BaseTableFetcher<Image> {

    @Autowired
    private ImagesAware imagesAware;

    @Override
    protected Class<Image> getBeanClass() {
        return Image.class;
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.IMAGES;
    }

    @Override
    protected Collection<Image> getRawData() {
        return imagesAware.getImages();
    }
}
