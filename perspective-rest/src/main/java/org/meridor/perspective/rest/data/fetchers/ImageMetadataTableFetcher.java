package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ImageMetadata;
import org.meridor.perspective.rest.data.converters.ImageConverters;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class ImageMetadataTableFetcher extends BaseTableFetcher<ImageMetadata> {

    @Autowired
    private ImagesAware imagesAware;

    @Override
    protected Class<ImageMetadata> getBeanClass() {
        return ImageMetadata.class;
    }

    @Override
    public String getTableName() {
        return TableName.IMAGE_METADATA.getTableName();
    }

    @Override
    protected Collection<ImageMetadata> getRawData() {
        return imagesAware.getImages().stream()
                .flatMap(ImageConverters::imageToMetadata)
                .collect(Collectors.toList());
    }
}
