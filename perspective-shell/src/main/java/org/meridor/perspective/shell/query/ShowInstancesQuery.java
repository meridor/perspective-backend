package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.SupportedCloud;
import org.meridor.perspective.shell.validator.SupportedInstanceState;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.*;

public class ShowInstancesQuery implements Query<Predicate<Instance>> {

    @Filter(INSTANCE_IDS)
    private Set<String> ids;

    @Filter(INSTANCE_NAMES)
    private Set<String> names;
    
    @Filter(FLAVOR_NAMES)
    private Set<String> flavors;
    
    @Filter(IMAGE_NAMES)
    private Set<String> images;
    
    @SupportedInstanceState
    @Filter(INSTANCE_STATES)
    private Set<String> states;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    public ShowInstancesQuery(String id, String name) {
        this(id, name, null);
    }
    
    public ShowInstancesQuery(String id, String name, String cloud) {
        this(id, name, null, null, null, cloud);
    }
    
    public ShowInstancesQuery(String id, String name, String flavor, String image, String state, String cloud) {
        this.ids = parseEnumeration(id);
        this.names = parseEnumeration(name);
        this.flavors = parseEnumeration(flavor);
        this.images = parseEnumeration(image);
        this.states = parseEnumeration(state);
        this.clouds = parseEnumeration(cloud);
    }

    @Override
    public Predicate<Instance> getPayload() {
        return getInstancePredicate(
                Optional.ofNullable(ids),
                Optional.ofNullable(names),
                Optional.ofNullable(flavors),
                Optional.ofNullable(images),
                Optional.ofNullable(states),
                Optional.ofNullable(clouds)
        );
    }

    //TODO: we may probably want to add project names
    private Predicate<Instance> getInstancePredicate(
            Optional<Set<String>> ids,
            Optional<Set<String>> names,
            Optional<Set<String>> flavors,
            Optional<Set<String>> images,
            Optional<Set<String>> states,
            Optional<Set<String>> clouds
    ) {
        return instance ->
                ( !ids.isPresent() || ids.get().contains(instance.getId()) ) &&
                ( !names.isPresent() || names.get().contains(instance.getName()) ) &&
                ( !flavors.isPresent() || flavors.get().contains(instance.getFlavor().getName()) ) &&
                ( !images.isPresent() || images.get().contains(instance.getImage().getName()) ) &&
                ( !states.isPresent() || states.get().contains(instance.getState().value().toLowerCase()) ) &&
                ( !clouds.isPresent() || clouds.get().contains(instance.getCloudType().value().toLowerCase()));
    }

}
