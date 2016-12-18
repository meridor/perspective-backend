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
import static org.meridor.perspective.shell.common.validator.Field.CLOUDS;
import static org.meridor.perspective.shell.common.validator.Field.PROJECTS;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindProjectsRequest implements Request<Query> {

    private Set<String> id;
    
    @Pattern
    @Filter(PROJECTS)
    private Set<String> name;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> cloud;

    public FindProjectsRequest withNames(String names) {
        this.name = parseEnumeration(names);
        return this;
    }
    
    public FindProjectsRequest withIds(String ids) {
        this.id = parseEnumeration(ids);
        return this;
    }
    
    public FindProjectsRequest withClouds(String clouds) {
        this.cloud = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Query getQuery() {
        JoinClause joinClause = new SelectQuery()
                .all()
                .from()
                .table("projects")
                .innerJoin()
                .table("project_quota")
                .on()
                .equal("projects.id", "project_quota.project_id");
        Map<String, Collection<String>> whereMap = new HashMap<>();
        if (id != null) {
            whereMap.put("id", id);
        }
        if (name != null) {
            whereMap.put("name", name);
        }
        if (cloud != null) {
            whereMap.put("cloud_type", cloud);
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("name")
                        .getQuery();
    }

}
