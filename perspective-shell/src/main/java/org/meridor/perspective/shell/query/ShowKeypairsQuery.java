package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.oneOfMatches;
import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ShowKeypairsQuery implements Query<Predicate<Keypair>> {
    
    @Filter(KEYPAIR_NAMES)
    private Set<String> names;

    @SupportedCloud
    @Filter(CLOUDS)
    private String clouds;

    @Filter(PROJECTS)
    private String projects;

    public ShowKeypairsQuery withNames(String name) {
        this.names = parseEnumeration(name);
        return this;
    }

    public ShowKeypairsQuery withProjects(String projects) {
        this.projects = projects;
        return this;
    }

    public ShowKeypairsQuery withClouds(String clouds) {
        this.clouds = clouds;
        return this;
    }

    public String getClouds() {
        return clouds;
    }

    public String getProjects() {
        return projects;
    }

    @Override
    public Predicate<Keypair> getPayload() {
        return getKeypairPredicate(Optional.ofNullable(names));
    }

    private Predicate<Keypair> getKeypairPredicate(Optional<Set<String>> name) {
        return keypair -> (!name.isPresent() || oneOfMatches(keypair.getName(), name.get()) );
    }

}
