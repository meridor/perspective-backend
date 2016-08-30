package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
public class ListProjectsOperation implements SupplyingOperation<Project> {

    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        consumer.accept(EntityGenerator.getProject());
        return true;
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Project> consumer) {
        Project project = EntityGenerator.getProject();
        if (ids.contains(project.getId())) {
            consumer.accept(project);
        }
        return true;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }
}
