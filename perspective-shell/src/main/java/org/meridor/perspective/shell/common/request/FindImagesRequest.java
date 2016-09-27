package org.meridor.perspective.shell.common.request;

import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.meridor.perspective.shell.common.validator.annotation.Pattern;
import org.meridor.perspective.shell.common.validator.annotation.SupportedCloud;
import org.meridor.perspective.shell.common.validator.annotation.SupportedImageState;
import org.meridor.perspective.sql.JoinClause;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.common.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindImagesRequest implements Request<Query> {
    
    private Set<String> ids;
    
    @Pattern
    @Filter(IMAGE_NAMES)
    private Set<String> names;

    @SupportedImageState
    @Filter(IMAGE_STATES)
    private Set<String> states;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> projects;
    
    public FindImagesRequest withIds(String imageIds) {
        this.ids = parseEnumeration(imageIds);
        return this;
    }
    
    public FindImagesRequest withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }

    public FindImagesRequest withStates(String states) {
        this.states = parseEnumeration(states);
        return this;
    }

    public FindImagesRequest withClouds(String cloudNames) {
        this.clouds = parseEnumeration(cloudNames);
        return this;
    }

    public FindImagesRequest withProjects(String projectNames) {
        this.projects = parseEnumeration(projectNames);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Query getQuery() {
        Optional<Set<String>> ids = Optional.ofNullable(this.ids);
        Optional<Set<String>> names = Optional.ofNullable(this.names);
        Optional<Set<String>> states = Optional.ofNullable(this.states);
        Optional<Set<String>> clouds = Optional.ofNullable(this.clouds);
        Optional<Set<String>> projects = Optional.ofNullable(this.projects);
        JoinClause joinClause = new SelectQuery()
                .columns(
                        "images.id",
                        "images.real_id",
                        "images.name",
                        "images.cloud_type",
                        "images.state",
                        "images.last_updated",
                        "projects.id",
                        "projects.name"
                )
                .from()
                .table("images")
                .innerJoin()
                    .table("project_images")
                    .on()
                    .equal("images.id", "project_images.image_id")
                .innerJoin()
                    .table("projects")
                    .on()
                    .equal("projects.id", "project_images.project_id");
        
        Map<String, Collection<String>> whereMap = new HashMap<>();
        
        if (ids.isPresent()) {
            whereMap.put("images.id", ids.get());
        }
        if (names.isPresent()) {
            whereMap.put("images.name", names.get());
        }
        if (states.isPresent()) {
            whereMap.put("images.state", states.get());
        }
        if (clouds.isPresent()) {
            whereMap.put("images.cloud_type", clouds.get());
        }
        if (projects.isPresent()) {
            whereMap.put("projects.name", projects.get());
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("images.name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("images.name")
                        .getQuery();
    }

}
