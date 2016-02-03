package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.oneOfMatches;
import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.CLOUDS;
import static org.meridor.perspective.shell.validator.Field.PROJECTS;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ShowProjectsQuery implements Query<Predicate<Project>> {
    
    @Filter(PROJECTS)
    private Set<String> names;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    public ShowProjectsQuery withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }
    
    public ShowProjectsQuery withClouds(String clouds) {
        this.clouds = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Predicate<Project> getPayload() {
        return getProjectPredicate(Optional.ofNullable(names), Optional.ofNullable(clouds));
    }

    private Predicate<Project> getProjectPredicate(Optional<Set<String>> projectNames, Optional<Set<String>> clouds) {
        return project ->
                ( !projectNames.isPresent() || oneOfMatches(project.getName(), projectNames.get()) ) &&
                ( !clouds.isPresent() || oneOfMatches(project.getCloudType().value().toLowerCase(), clouds.get()) );
    }

}
