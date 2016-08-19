package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ImageMetadata;
import org.meridor.perspective.rest.data.converters.ImageConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class ImageMetadataTableFetcher extends ImagesBasedTableFetcher<ImageMetadata> {

    @Override
    protected Class<ImageMetadata> getBeanClass() {
        return ImageMetadata.class;
    }

    @Override
    public String getTableName() {
        return TableName.IMAGE_METADATA.getTableName();
    }

    @Override
    protected Predicate<Image> getPredicate(String id) {
        String[] pieces = parseCompositeId(id, 2);
        String imageId = pieces[0];
        return i -> imageId.equals(i.getId());

    }

    @Override
    protected Function<Image, Stream<ImageMetadata>> getConverter() {
        return ImageConverters::imageToMetadata;
    }
}
