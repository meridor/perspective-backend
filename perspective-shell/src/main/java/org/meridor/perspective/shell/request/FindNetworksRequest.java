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
public class FindNetworksRequest implements Request<Query> {
    
    @Pattern
    @Filter(NETWORK_NAMES)
    private Set<String> names;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> projects;

    public FindNetworksRequest withNames(String name) {
        this.names = parseEnumeration(name);
        return this;
    }

    public FindNetworksRequest withProjects(String projects) {
        this.projects = parseEnumeration(projects);
        return this;
    }

    public FindNetworksRequest withClouds(String clouds) {
        this.clouds = parseEnumeration(clouds);
        return this;
    }

    @Override
    public Query getPayload() {
        return getNetworkQuery(Optional.ofNullable(names), Optional.ofNullable(clouds), Optional.ofNullable(projects));
    }

    private Query getNetworkQuery(Optional<Set<String>> names, Optional<Set<String>> clouds, Optional<Set<String>> projects) {
        JoinClause joinClause = new SelectQuery()
                .columns(
                        "networks.id",
                        "networks.name",
                        "projects.name",
                        "networks.state",
                        "networks.is_shared",
                        "network_subnets.cidr"
                )
                .from()
                .table("networks")
                .innerJoin()
                    .table("projects")
                    .on()
                    .equal("networks.project_id", "projects.id")
                .innerJoin()
                    .table("network_subnets")
                    .on()
                    .and(new HashMap<String, String>(){
                        {
                            put("network_subnets.project_id", "projects.id");
                            put("network_subnets.network_id", "networks.id");
                        }
                    });
        Map<String, Collection<String>> whereMap = new HashMap<>();
        if (names.isPresent()) {
            whereMap.put("networks.name", names.get());
        }
        if (clouds.isPresent()) {
            whereMap.put("projects.cloud_type", clouds.get());
        }
        if (projects.isPresent()) {
            whereMap.put("projects.name", projects.get());
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("networks.name")
                        .getQuery() :
                joinClause
                        .where().and(whereMap)
                        .orderBy().column("networks.name")
                        .getQuery();
    }

}
