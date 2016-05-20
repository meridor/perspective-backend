package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ProjectMetadata;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class ProjectMetadataTableFetcher extends BaseTableFetcher<ProjectMetadata> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<ProjectMetadata> getBeanClass() {
        return ProjectMetadata.class;
    }

    @Override
    public String getTableName() {
        return TableName.PROJECT_METADATA.getTableName();
    }

    @Override
    protected Collection<ProjectMetadata> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(ProjectConverters::projectToMetadata)
                .collect(Collectors.toList());
    }
}
