package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.repository.query.validator.Filter;
import org.meridor.perspective.shell.repository.query.validator.SupportedCloud;

import java.util.Optional;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.query.validator.Field.CLOUD;

public class ShowImagesQuery extends BaseQuery<Predicate<Image>> {
    
    private String id;
    
    private String name;
    
    @SupportedCloud
    @Filter(CLOUD)
    private String cloud;


    public ShowImagesQuery(String id, String name, String cloud) {
        this.id = id;
        this.name = name;
        this.cloud = cloud;
    }

    @Override
    public Predicate<Image> getPayload() {
        return getImagePredicate(
                Optional.ofNullable(id),
                Optional.ofNullable(name),
                Optional.ofNullable(cloud)
        );
    }

    private Predicate<Image> getImagePredicate(
            Optional<String> id,
            Optional<String> name,
            Optional<String> cloud
    ) {
        return image -> 
                ( !id.isPresent() || image.getId().contains(id.get()) || image.getId().matches(id.get()) ) &&
                ( !name.isPresent() || image.getName().contains(name.get()) || image.getName().matches(name.get()) ) &&
                ( !cloud.isPresent() || image.getCloudType().value().contains(cloud.get()));
    }

}
