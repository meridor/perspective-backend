package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;
import static org.meridor.perspective.events.EventFactory.now;

@Component
public class ListProjectsOperation implements SupplyingOperation<Project> {

    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        consumer.accept(getProject());
        return true;
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Project> consumer) {
        Project project = getProject();
        if (ids.contains(project.getId())) {
            consumer.accept(project);
        }
        return true;
    }

    private Project getProject() {
        Project project = EntityGenerator.getProject();
        project.setTimestamp(now().minusHours(1));
        return project;
    }
    
    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }
}
