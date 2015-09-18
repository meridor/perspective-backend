package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Network;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.validator.Field.CLOUD;
import static org.meridor.perspective.shell.validator.Field.PROJECT;

public class ShowNetworksQuery implements Query<Predicate<Network>> {
    
    private Set<String> names;
    
    @Filter(PROJECT)
    private Set<String> projectNames;

    @SupportedCloud
    @Filter(CLOUD)
    private Set<String> clouds;

    public ShowNetworksQuery(Set<String> names, Set<String> projectNames, Set<String> clouds) {
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
    public Predicate<Network> getPayload() {
        return getNetworkPredicate(Optional.ofNullable(names));
    }

    private Predicate<Network> getNetworkPredicate(Optional<Set<String>> name) {
        return network -> (!name.isPresent() || name.get().contains(network.getName()));
    }

}
