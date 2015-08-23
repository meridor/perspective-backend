package org.meridor.perspective.framework.storage;

import org.meridor.perspective.beans.Project;

import java.util.Collection;
import java.util.Optional;

public interface ProjectsAware {

    Optional<Project> getProject(String projectId);

    Collection<Project> getProjects(Optional<String> query) throws IllegalQueryException;

    void saveProject(Project project);

}
