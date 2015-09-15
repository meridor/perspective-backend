package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.beans.Network;
import org.meridor.perspective.shell.repository.query.validator.SupportedCloud;
import org.meridor.perspective.shell.repository.query.validator.Filter;

import java.util.Optional;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.query.validator.Field.CLOUD;
import static org.meridor.perspective.shell.repository.query.validator.Field.PROJECT;

public class ShowNetworksQuery extends BaseQuery<Predicate<Network>> {
    
    private String name;
    
    @Filter(PROJECT)
    private String projectName;

    @SupportedCloud
    @Filter(CLOUD)
    private String cloud;

    public ShowNetworksQuery(String name, String projectName, String cloud) {
        this.name = name;
        this.projectName = projectName;
        this.cloud = cloud;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getCloud() {
        return cloud;
    }

    @Override
    public Predicate<Network> getPayload() {
        return getNetworkPredicate(Optional.ofNullable(name));
    }

    private Predicate<Network> getNetworkPredicate(Optional<String> name) {
        return network -> (!name.isPresent() || network.getName().contains(name.get()));
    }

}
