package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Network;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.CLOUDS;
import static org.meridor.perspective.shell.validator.Field.NETWORK_NAMES;
import static org.meridor.perspective.shell.validator.Field.PROJECTS;

public class ShowNetworksQuery implements Query<Predicate<Network>> {
    
    @Filter(NETWORK_NAMES)
    private Set<String> names;
    
    @Filter(PROJECTS)
    private Set<String> projectNames;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    public ShowNetworksQuery(String name, String projectName, String cloud) {
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
    public Predicate<Network> getPayload() {
        return getNetworkPredicate(Optional.ofNullable(names));
    }

    private Predicate<Network> getNetworkPredicate(Optional<Set<String>> name) {
        return network -> (!name.isPresent() || name.get().contains(network.getName()));
    }

}
