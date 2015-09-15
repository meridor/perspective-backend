package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.shell.repository.query.validator.SupportedCloud;
import org.meridor.perspective.shell.repository.query.validator.Filter;

import java.util.Optional;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.query.validator.Field.CLOUD;
import static org.meridor.perspective.shell.repository.query.validator.Field.PROJECT;

public class ShowFlavorsQuery extends BaseQuery<Predicate<Flavor>> {
    
    private String name;
    
    @Filter(PROJECT)
    private String projectName;
    
    @SupportedCloud
    @Filter(CLOUD)
    private String cloud;

    public ShowFlavorsQuery(String name, String projectName, String cloud) {
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
    public Predicate<Flavor> getPayload() {
        return getFlavorPredicate(Optional.ofNullable(name));
    }

    private Predicate<Flavor> getFlavorPredicate(Optional<String> name) {
        return flavor -> (!name.isPresent() || flavor.getName().contains(name.get()));
    }

}
