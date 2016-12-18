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
public class FindKeypairsRequest implements Request<Query> {
    
    @Pattern
    @Filter(KEYPAIR_NAMES)
    private Set<String> name;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> cloud;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> project;

    public FindKeypairsRequest withNames(String name) {
        this.name = parseEnumeration(name);
        return this;
    }

    public FindKeypairsRequest withProjects(String projects) {
        this.project = parseEnumeration(projects);
        return this;
    }

    public FindKeypairsRequest withClouds(String clouds) {
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
                        "keypairs.name",
                        "keypairs.fingerprint",
                        "projects.name"
                )
                .from()
                .table("keypairs")
                .innerJoin()
                .table("projects")
                .on()
                .equal("keypairs.project_id", "projects.id");
        Map<String, Collection<String>> whereMap = new HashMap<>();

        if (name != null) {
            whereMap.put("keypairs.name", name);
        }
        if (cloud != null) {
            whereMap.put("projects.cloud_type", cloud);
        }
        if (project != null) {
            whereMap.put("projects.name", project);
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("keypairs.name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("keypairs.name")
                        .getQuery();
    }

}
