package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;
import static org.meridor.perspective.mock.EntityGenerator.getProject;

@Component
@Operation(cloud = MOCK, type = LIST_PROJECTS)
public class ListProjectsOperation {
    
    @EntryPoint
    public boolean listProjects(Cloud cloud, Consumer<Set<Project>> consumer) {
        Set<Project> projects = new HashSet<>();
        projects.add(getProject());
        consumer.accept(projects);
        return true;
    }
    
    
}
