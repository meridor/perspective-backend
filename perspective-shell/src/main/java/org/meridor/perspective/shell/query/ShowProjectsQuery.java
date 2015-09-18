package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.validator.Filter;
import org.meridor.perspective.shell.validator.SupportedCloud;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.CLOUDS;

public class ShowProjectsQuery implements Query<Predicate<Project>> {
    
    private Set<String> names;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    public ShowProjectsQuery(String name, String cloud) {
        this.names = parseEnumeration(name);
        this.clouds = parseEnumeration(cloud);
    }

    @Override
    public Predicate<Project> getPayload() {
        return getProjectPredicate(Optional.ofNullable(names), Optional.ofNullable(clouds));
    }

    private Predicate<Project> getProjectPredicate(Optional<Set<String>> projectNames, Optional<Set<String>> clouds) {
        return project ->
                ( !projectNames.isPresent() || projectNames.get().contains(project.getName()) ) &&
                        ( !clouds.isPresent() || clouds.get().contains(project.getCloudType().value().toLowerCase()));
    }

}
