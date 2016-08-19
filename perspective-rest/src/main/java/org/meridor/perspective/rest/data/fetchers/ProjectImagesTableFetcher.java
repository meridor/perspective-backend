package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ProjectImage;
import org.meridor.perspective.rest.data.converters.ImageConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class ProjectImagesTableFetcher extends ImagesBasedTableFetcher<ProjectImage> {

    @Override
    protected Class<ProjectImage> getBeanClass() {
        return ProjectImage.class;
    }

    @Override
    public String getTableName() {
        return TableName.PROJECT_IMAGES.getTableName();
    }

    @Override
    protected Predicate<Image> getPredicate(String id) {
        String[] pieces = parseCompositeId(id, 2);
        String imageId = pieces[1];
        return i -> imageId.equals(i.getId());

    }

    @Override
    protected Function<Image, Stream<ProjectImage>> getConverter() {
        return ImageConverters::imageToProjectImages;
    }
}
