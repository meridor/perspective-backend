package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.query.validator.Field;
import org.meridor.perspective.shell.repository.query.validator.Filter;
import org.meridor.perspective.shell.repository.query.validator.Required;
import org.meridor.perspective.shell.repository.query.validator.SupportedCloud;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;

public class DeleteImagesQuery extends BaseQuery<List<Image>> {

    private ImagesRepository imagesRepository;

    @Required
    private String names;
    
    @Filter(Field.CLOUD)
    @SupportedCloud
    private String cloud;

    public DeleteImagesQuery(String names, String cloud, ImagesRepository imagesRepository) {
        this.names = names;
        this.cloud = cloud;
        this.imagesRepository = imagesRepository;
    }

    @Override
    public List<Image> getPayload() {
        String[] tokens = parseEnumeration(names);
        return Arrays.stream(tokens).flatMap(t -> {
            ShowImagesQuery showImagesQuery = new ShowImagesQuery(t, t, cloud);
            return imagesRepository.showImages(showImagesQuery).stream();
        }).collect(Collectors.toList());
    }
}
