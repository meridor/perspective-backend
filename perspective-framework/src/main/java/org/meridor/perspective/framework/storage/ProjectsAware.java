package org.meridor.perspective.framework.storage;

import org.meridor.perspective.beans.Project;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface ProjectsAware {

    Optional<Project> getProject(String projectId);

    Collection<Project> getProjects();
    
    Collection<Project> getProjects(Set<String> ids);
    
    Collection<Project> getProjects(Predicate<Project> predicate);

    void saveProject(Project project);

    void addProjectListener(EntityListener<Project> listener);
    
}
