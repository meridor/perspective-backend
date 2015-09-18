package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.validator.Field.CLOUD;
import static org.meridor.perspective.shell.validator.Field.PROJECT;

public class ShowFlavorsQuery implements Query<Predicate<Flavor>> {
    
    private Set<String> names;
    
    @Filter(PROJECT)
    private Set<String> projectNames;
    
    @SupportedCloud
    @Filter(CLOUD)
    private Set<String> clouds;

    public ShowFlavorsQuery(Set<String> names, Set<String> projectNames, Set<String> clouds) {
        this.names = names;
        this.projectNames = projectNames;
        this.clouds = clouds;
    }

    public Set<String> getProjectNames() {
        return projectNames;
    }

    public Set<String> getClouds() {
        return clouds;
    }

    @Override
    public Predicate<Flavor> getPayload() {
        return getFlavorPredicate(Optional.ofNullable(names));
    }

    private Predicate<Flavor> getFlavorPredicate(Optional<Set<String>> names) {
        return flavor -> (!names.isPresent() || names.get().contains(flavor.getName()));
    }

}
