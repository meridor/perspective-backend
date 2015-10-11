package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;

public class DeleteImagesQuery implements Query<List<Image>> {

    private ImagesRepository imagesRepository;

    @Filter(Field.IMAGE_NAMES)
    @Required
    private Set<String> names;
    
    @Filter(Field.CLOUDS)
    @SupportedCloud
    private String cloud;

    public DeleteImagesQuery(String names, String cloud, ImagesRepository imagesRepository) {
        this.names = parseEnumeration(names);
        this.cloud = cloud;
        this.imagesRepository = imagesRepository;
    }

    @Override
    public List<Image> getPayload() {
        return names.stream().flatMap(t -> {
            ShowImagesQuery showImagesQuery = new ShowImagesQuery(t, t, cloud);
            return imagesRepository.showImages(showImagesQuery).stream();
        }).collect(Collectors.toList());
    }
}
