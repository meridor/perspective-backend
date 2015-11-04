package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.validator.Field;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class DeleteImagesQuery implements Query<List<Image>> {

    @Autowired
    private ImagesRepository imagesRepository;

    @Filter(Field.IMAGE_NAMES)
    @Required
    private Set<String> names;
    
    @Filter(Field.CLOUDS)
    @SupportedCloud
    private String clouds;

    @Autowired
    private QueryProvider queryProvider;

    public DeleteImagesQuery withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }
    
    public DeleteImagesQuery withClouds(String clouds) {
        this.clouds = clouds;
        return this;
    }

    @Override
    public List<Image> getPayload() {
        return names.stream().flatMap(n -> {
            ShowImagesQuery showImagesQuery = queryProvider.get(ShowImagesQuery.class)
                    .withNames(n)
                    .withCloudNames(clouds);
            return imagesRepository.showImages(showImagesQuery).stream();
        }).collect(Collectors.toList());
    }
}
