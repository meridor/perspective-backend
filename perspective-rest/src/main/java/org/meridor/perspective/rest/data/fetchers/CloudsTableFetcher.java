package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.Cloud;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CloudsTableFetcher extends BaseTableFetcher<Cloud> {

    @Autowired
    private ProjectsAware projectsAware;
    
    @Override
    protected Class<Cloud> getBeanClass() {
        return Cloud.class;
    }

    @Override
    public String getTableName() {
        return TableName.CLOUDS.getTableName();
    }

    @Override
    protected Collection<Cloud> getAllRawEntities() {
        return projectsAware.getProjects().stream()
                .flatMap(ProjectConverters::projectToCloud)
                .collect(Collectors.toList());
    }

    @Override
    protected Collection<Cloud> getRawEntities(Set<String> ids) {
        return projectsAware.getProjects().stream()
                .filter(p -> ids.contains(p.getCloudId()))
                .flatMap(ProjectConverters::projectToCloud)
                .collect(Collectors.toList());
    }

}
