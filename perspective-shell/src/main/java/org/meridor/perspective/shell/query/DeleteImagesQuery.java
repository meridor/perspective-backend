package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.Required;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;

public class DeleteImagesQuery implements Query<List<Image>> {

    private ImagesRepository imagesRepository;

    @Filter(Field.IMAGE_NAMES)
    @Required
    private String names;
    
    @Filter(Field.CLOUDS)
    @SupportedCloud
    private String cloud;

    public DeleteImagesQuery(String names, String cloud, ImagesRepository imagesRepository) {
        this.names = names;
        this.cloud = cloud;
        this.imagesRepository = imagesRepository;
    }

    @Override
    public List<Image> getPayload() {
        Set<String> tokens = parseEnumeration(names);
        return tokens.stream().flatMap(t -> {
            ShowImagesQuery showImagesQuery = new ShowImagesQuery(t, t, cloud);
            return imagesRepository.showImages(showImagesQuery).stream();
        }).collect(Collectors.toList());
    }
}
