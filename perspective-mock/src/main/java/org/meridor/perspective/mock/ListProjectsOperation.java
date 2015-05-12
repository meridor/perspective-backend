package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;
import static org.meridor.perspective.mock.EntityGenerator.getProject;

@Component
@Operation(cloud = MOCK, type = LIST_PROJECTS)
public class ListProjectsOperation {
    
    @EntryPoint
    public void listProjects(List<Project> projects) {
        projects.add(getProject());
    }
    
    
}
