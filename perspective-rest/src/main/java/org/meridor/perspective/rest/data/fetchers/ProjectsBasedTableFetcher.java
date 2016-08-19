package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.sql.impl.storage.impl.DerivedTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public abstract class ProjectsBasedTableFetcher<T> extends DerivedTableFetcher<Project, T> {
    
    @Autowired
    private ProjectsAware projectsAware;
    
    @Override
    protected Function<Predicate<Project>, Collection<Project>> getPredicateFetcher() {
        return predicate -> projectsAware.getProjects(predicate);
    }

    @Override
    protected Collection<Project> getAllBaseEntities() {
        return projectsAware.getProjects();
    }
}
