package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.*;

public class ShowImagesQuery implements Query<Predicate<Image>> {
    
    @Filter(IMAGE_IDS)
    private Set<String> ids;
    
    @Filter(IMAGE_NAMES)
    private Set<String> names;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;


    public ShowImagesQuery(String id, String name, String cloud) {
        this.ids = parseEnumeration(id);
        this.names = parseEnumeration(name);
        this.clouds = parseEnumeration(cloud);
    }

    @Override
    public Predicate<Image> getPayload() {
        return getImagePredicate(
                Optional.ofNullable(ids),
                Optional.ofNullable(names),
                Optional.ofNullable(clouds)
        );
    }

    private Predicate<Image> getImagePredicate(
            Optional<Set<String>> id,
            Optional<Set<String>> name,
            Optional<Set<String>> cloud
    ) {
        return image -> 
                ( !id.isPresent() || id.get().contains(image.getId()) ) &&
                ( !name.isPresent() || name.get().contains(image.getName()) ) &&
                ( !cloud.isPresent() || cloud.get().contains(image.getCloudType().value().toLowerCase()));
    }

}
