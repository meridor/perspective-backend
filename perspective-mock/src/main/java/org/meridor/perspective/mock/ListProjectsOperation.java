package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;
import static org.meridor.perspective.mock.EntityGenerator.getProject;

@Component
public class ListProjectsOperation implements SupplyingOperation<Set<Project>> {

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Project>> consumer) {
        Set<Project> projects = new HashSet<>();
        projects.add(getProject());
        consumer.accept(projects);
        return true;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }
}
