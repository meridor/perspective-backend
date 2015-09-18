package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.CLOUDS;
import static org.meridor.perspective.shell.validator.Field.FLAVOR_NAMES;
import static org.meridor.perspective.shell.validator.Field.PROJECTS;

public class ShowFlavorsQuery implements Query<Predicate<Flavor>> {
    
    @Filter(FLAVOR_NAMES)
    private Set<String> names;
    
    @Filter(PROJECTS)
    private Set<String> projectNames;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    public ShowFlavorsQuery(String name, String projectName, String cloud) {
        this.names = parseEnumeration(name);
        this.projectNames = parseEnumeration(projectName);
        this.clouds = parseEnumeration(cloud);
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
