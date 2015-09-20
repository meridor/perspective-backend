package org.meridor.perspective.docker;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
public class ListProjectsOperation implements SupplyingOperation<Set<Project>> {

    private static Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Project>> consumer) {
        Set<Project> projects = new HashSet<>();
        Project project = createProject(cloud);
        projects.add(project);
        consumer.accept(projects);
        LOG.debug("Fetched {} projects", projects.size());
        return true;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }

    private Project createProject(Cloud cloud) {
        Project project = new Project();
        project.setId(cloud.getId());
        project.setName(cloud.getName());
        project.setTimestamp(ZonedDateTime.now());
        return project;
    }
}
