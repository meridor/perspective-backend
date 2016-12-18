package org.meridor.perspective.shell.common.request;

import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.shell.common.validator.Setting;
import org.meridor.perspective.shell.common.validator.annotation.Filter;
import org.meridor.perspective.shell.common.validator.annotation.Pattern;
import org.meridor.perspective.shell.common.validator.annotation.SupportedCloud;
import org.meridor.perspective.shell.common.validator.annotation.SupportedInstanceState;
import org.meridor.perspective.sql.JoinClause;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.removeSuffixes;
import static org.meridor.perspective.shell.common.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class FindInstancesRequest implements Request<Query> {


    private Set<String> id;

    @Pattern
    @Filter(INSTANCE_NAMES)
    private Set<String> name;
    
    @Pattern
    @Filter(FLAVOR_NAMES)
    private Set<String> flavor;
    
    @Pattern
    @Filter(IMAGE_NAMES)
    private Set<String> image;
    
    @SupportedInstanceState
    @Filter(INSTANCE_STATES)
    private Set<String> state;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> cloud;

    @Pattern
    @Filter(PROJECTS)
    private Set<String> project;

    private final SettingsAware settingsAware;

    @Autowired
    public FindInstancesRequest(SettingsAware settingsAware) {
        this.settingsAware = settingsAware;
    }

    public FindInstancesRequest withIds(String ids) {
        this.id = parseEnumeration(ids);
        return this;
    }
    
    public FindInstancesRequest withNames(String names) {
        this.name = parseEnumeration(names);
        return this;
    }
    
    public FindInstancesRequest withClouds(String clouds) {
        this.cloud = parseEnumeration(clouds);
        return this;
    }

    public FindInstancesRequest withProjects(String projects) {
        this.project = parseEnumeration(projects);
        return this;
    }
    
    public FindInstancesRequest withImages(String images) {
        this.image = parseEnumeration(images);
        return this;
    }
    
    public FindInstancesRequest withFlavors(String flavors) {
        this.flavor = parseEnumeration(flavors);
        return this;
    }
    
    public FindInstancesRequest withStates(String states) {
        this.state = parseEnumeration(states);
        return this;
    }

    @Override
    public Query getPayload() {
        return getQuery();
    }

    private Set<String> getInstanceSuffixes() {
        if (settingsAware.hasSetting(Setting.INSTANCE_SUFFIXES)) {
            return settingsAware.getSetting(Setting.INSTANCE_SUFFIXES);
        }
        return Collections.emptySet();
    }
    
    private Query getQuery() {
        Collection<String> names = removeSuffixes(this.name, getInstanceSuffixes());
        JoinClause joinClause = new SelectQuery()
                .columns(
                        "instances.id",
                        "instances.real_id",
                        "instances.name",
                        "projects.id",
                        "projects.name",
                        "instances.cloud_id",
                        "instances.cloud_type",
                        "images.name",
                        "flavors.name",
                        "instances.addresses",
                        "instances.state",
                        "instances.last_updated"
                )
                .from()
                .table("instances")
                .innerJoin()
                    .table("projects")
                    .on()
                    .equal("instances.project_id", "projects.id")
                .leftJoin()
                    .table("flavors")
                    .on()
                    .and(new HashMap<String, String>(){
                        {
                            put("instances.flavor_id", "flavors.id");
                            put("instances.project_id", "flavors.project_id");
                        }
                    })
                .leftJoin()
                    .table("images")
                    .on()
                    .equal("instances.image_id", "images.id");
        
        Map<String, Collection<String>> whereMap = new HashMap<>();
        if (id != null) {
            whereMap.put("instances.id", id);
        }
        if (names != null) {
            whereMap.put("instances.name", names);
        }
        if (flavor != null) {
            whereMap.put("flavors.name", flavor);
        }
        if (image != null) {
            whereMap.put("images.name", image);
        }
        if (state != null) {
            whereMap.put("instances.state", state);
        }
        if (cloud != null) {
            whereMap.put("instances.cloud_type", cloud);
        }
        if (project != null) {
            whereMap.put("projects.name", project);
        }
        return whereMap.isEmpty() ?
                joinClause
                        .orderBy().column("instances.name")
                        .getQuery() :
                joinClause
                        .where().matches(whereMap)
                        .orderBy().column("instances.name")
                        .getQuery();
    }
    
}
