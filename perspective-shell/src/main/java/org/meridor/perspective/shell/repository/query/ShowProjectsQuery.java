package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.repository.query.validator.SupportedCloud;
import org.meridor.perspective.shell.repository.query.validator.Filter;

import java.util.Optional;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.query.validator.Field.CLOUD;

public class ShowProjectsQuery extends BaseQuery<Predicate<Project>> {
    
    private String name;
    
    @SupportedCloud
    @Filter(CLOUD)
    private String cloud;

    public ShowProjectsQuery(String name, String cloud) {
        this.name = name;
        this.cloud = cloud;
    }

    @Override
    public Predicate<Project> getPayload() {
        return getProjectPredicate(Optional.ofNullable(name), Optional.ofNullable(cloud));
    }

    private Predicate<Project> getProjectPredicate(Optional<String> projectName, Optional<String> cloud) {
        return project ->
                ( !projectName.isPresent() || project.getName().contains(projectName.get()) ) &&
                        ( !cloud.isPresent() || project.getCloudType().value().contains(cloud.get().toUpperCase()) );
    }

}
