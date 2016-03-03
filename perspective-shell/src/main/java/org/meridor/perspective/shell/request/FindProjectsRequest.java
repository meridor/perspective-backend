package org.meridor.perspective.shell.request;

import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.Pattern;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.meridor.perspective.sql.FromClause;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.CLOUDS;
import static org.meridor.perspective.shell.validator.Field.PROJECTS;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindProjectsRequest implements Request<Query> {
    
    @Pattern
    @Filter(PROJECTS)
    private Set<String> names;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    public FindProjectsRequest withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }
    
    public FindProjectsRequest withClouds(String clouds) {
        this.clouds = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getProjectQuery(Optional.ofNullable(names), Optional.ofNullable(clouds));
    }

    private Query getProjectQuery(Optional<Set<String>> projectNames, Optional<Set<String>> clouds) {
        FromClause fromClause = new SelectQuery()
                .all()
                .from()
                .table("projects");
        Map<String, Collection<String>> whereMap = new HashMap<>();
        if (projectNames.isPresent()) {
            whereMap.put("name", projectNames.get());
        }
        if (clouds.isPresent()) {
            whereMap.put("cloud_type", clouds.get());
        }
        return whereMap.isEmpty() ? 
                fromClause.getQuery() :
                fromClause.where().or(whereMap).getQuery();
    }

}
