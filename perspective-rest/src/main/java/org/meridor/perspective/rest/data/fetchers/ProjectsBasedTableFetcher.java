package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.sql.impl.storage.impl.DerivedTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public abstract class ProjectsBasedTableFetcher<T> extends DerivedTableFetcher<Project, T> {
    
    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Collection<Project> getBaseEntities(Set<String> ids) {
        return projectsAware.getProjects(ids);
    }

    @Override
    protected Collection<Project> getAllBaseEntities() {
        return projectsAware.getProjects();
    }
}
