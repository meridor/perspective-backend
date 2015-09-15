package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.query.validator.SupportedCloud;
import org.meridor.perspective.shell.repository.query.validator.Filter;
import org.meridor.perspective.shell.repository.query.validator.SupportedInstanceState;

import java.util.Optional;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.query.validator.Field.*;

public class ShowInstancesQuery extends BaseQuery<Predicate<Instance>> {
    
    private String name;
    
    @Filter(FLAVOR)
    private String flavor;
    
    @Filter(IMAGE)
    private String image;
    
    @SupportedInstanceState
    @Filter(INSTANCE_STATE)
    private String state;
    
    @SupportedCloud
    @Filter(CLOUD)
    private String cloud;

    public ShowInstancesQuery(String name, String flavor, String image, String state, String cloud) {
        this.name = name;
        this.flavor = flavor;
        this.image = image;
        this.state = state;
        this.cloud = cloud;
    }

    @Override
    public Predicate<Instance> getPayload() {
        return getInstancePredicate(
                Optional.ofNullable(name),
                Optional.ofNullable(flavor),
                Optional.ofNullable(image),
                Optional.ofNullable(state),
                Optional.ofNullable(cloud)
        );
    }

    //TODO: we may probably want to add project name
    private Predicate<Instance> getInstancePredicate(
            Optional<String> name,
            Optional<String> flavor,
            Optional<String> image,
            Optional<String> state,
            Optional<String> cloud
    ) {
        return instance ->
                ( !name.isPresent() || instance.getName().contains(name.get()) ) &&
                        ( !flavor.isPresent() || instance.getFlavor().getName().contains(flavor.get()) ) &&
                        ( !image.isPresent() || instance.getImage().getName().contains(image.get()) ) &&
                        ( !state.isPresent() || instance.getState().value().contains(state.get().toUpperCase()) ) &&
                        ( !cloud.isPresent() || instance.getCloudType().value().contains(cloud.get()));
    }

}
