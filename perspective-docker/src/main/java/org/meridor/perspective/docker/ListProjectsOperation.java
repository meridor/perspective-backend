package org.meridor.perspective.docker;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
public class ListProjectsOperation implements SupplyingOperation<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        Project project = createProject(cloud);
        consumer.accept(project);
        LOG.info("Fetched project {} for cloud = {}", project.getName(), cloud.getName());
        return true;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }

    private Project createProject(Cloud cloud) {
        Project project = new Project();
        project.setId(idGenerator.getProjectId(cloud));
        project.setName(cloud.getName());
        project.setTimestamp(ZonedDateTime.now());
        return project;
    }
}
