package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class ProjectsTableFetcher extends BaseTableFetcher<Project> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<Project> getBeanClass() {
        return Project.class;
    }

    @Override
    public String getTableName() {
        return TableName.PROJECTS.getTableName();
    }

    @Override
    protected Collection<Project> getRawEntities(Set<String> ids) {
        return projectsAware.getProjects(ids);
    }

    @Override
    protected Collection<Project> getAllRawEntities() {
        return projectsAware.getProjects();
    }
}
