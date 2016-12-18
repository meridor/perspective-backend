package org.meridor.perspective.shell.common.request;

import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.meridor.perspective.shell.common.validator.annotation.Pattern;
import org.meridor.perspective.shell.common.validator.annotation.SupportedCloud;
import org.meridor.perspective.sql.JoinClause;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.common.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindFlavorsRequest implements Request<Query> {
    
    @Pattern
    @Filter(FLAVOR_NAMES)
    private Set<String> name;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> cloud;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> project;

    public FindFlavorsRequest withNames(String name) {
        this.name = parseEnumeration(name);
        return this;
    }
    
    public FindFlavorsRequest withProjects(String projects) {
        this.project = parseEnumeration(projects);
        return this;
    }
    
    public FindFlavorsRequest withClouds(String clouds) {
        this.cloud = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Query getQuery() {
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
        if (name != null) {
            whereMap.put("flavors.name", name);
        }
        if (cloud != null) {
            whereMap.put("projects.cloud_type", cloud);
        }
        if (project != null) {
            whereMap.put("projects.name", project);
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
