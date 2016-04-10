package org.meridor.perspective.shell.request;

import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.Pattern;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.meridor.perspective.sql.JoinClause;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindFlavorsRequest implements Request<Query> {
    
    @Pattern
    @Filter(FLAVOR_NAMES)
    private Set<String> names;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> projects;

    public FindFlavorsRequest withNames(String name) {
        this.names = parseEnumeration(name);
        return this;
    }
    
    public FindFlavorsRequest withProjects(String projects) {
        this.projects = parseEnumeration(projects);
        return this;
    }
    
    public FindFlavorsRequest withClouds(String clouds) {
        this.clouds = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getFlavorQuery(Optional.ofNullable(names), Optional.ofNullable(clouds), Optional.ofNullable(projects));
    }

    private Query getFlavorQuery(Optional<Set<String>> names, Optional<Set<String>> clouds, Optional<Set<String>> projects) {
        JoinClause joinClause = new SelectQuery()
                .columns(
                        "flavors.id",
                        "flavors.name",
                        "projects.name",
                        "flavors.vcpus",
                        "flavors.ram",
                        "flavors.root_disk",
                        "flavors.ephemeral_disk"
                )
                .from()
                .table("flavors")
                .innerJoin()
                .table("projects")
                .on()
                .equal("flavors.project_id", "projects.id");
        Map<String, Collection<String>> whereMap = new HashMap<>();
        if (names.isPresent()) {
            whereMap.put("flavors.name", names.get());
        }
        if (clouds.isPresent()) {
            whereMap.put("projects.cloud_type", clouds.get());
        }
        if (projects.isPresent()) {
            whereMap.put("projects.name", projects.get());
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("flavors.name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("flavors.name")
                        .getQuery();
    }

}
