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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.common.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindImagesRequest implements Request<Query> {
    
    private Set<String> ids;
    
    @Pattern
    @Filter(IMAGE_NAMES)
    private Set<String> name;

    @SupportedImageState
    @Filter(IMAGE_STATES)
    private Set<String> state;

    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> cloud;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> project;
    
    public FindImagesRequest withIds(String imageIds) {
        this.ids = parseEnumeration(imageIds);
        return this;
    }
    
    public FindImagesRequest withNames(String names) {
        this.name = parseEnumeration(names);
        return this;
    }

    public FindImagesRequest withStates(String states) {
        this.state = parseEnumeration(states);
        return this;
    }

    public FindImagesRequest withClouds(String cloudNames) {
        this.cloud = parseEnumeration(cloudNames);
        return this;
    }

    public FindImagesRequest withProjects(String projectNames) {
        this.project = parseEnumeration(projectNames);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Query getQuery() {
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

        if (ids != null) {
            whereMap.put("images.id", ids);
        }
        if (name != null) {
            whereMap.put("images.name", name);
        }
        if (state != null) {
            whereMap.put("images.state", state);
        }
        if (cloud != null) {
            whereMap.put("images.cloud_type", cloud);
        }
        if (project != null) {
            whereMap.put("projects.name", project);
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
