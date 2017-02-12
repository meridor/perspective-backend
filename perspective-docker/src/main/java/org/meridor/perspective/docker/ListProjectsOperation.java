package org.meridor.perspective.docker;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.beans.Quota;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;
import static org.meridor.perspective.events.EventFactory.now;

@Component
public class ListProjectsOperation implements SupplyingOperation<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    private final IdGenerator idGenerator;

    @Autowired
    public ListProjectsOperation(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        Project project = createProject(cloud);
        consumer.accept(project);
        LOG.info("Fetched project {} for cloud = {}", project.getName(), cloud.getName());
        return true;
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Project> consumer) {
        LOG.warn("Not implemented. Doing full projects fetch instead.");
        return perform(cloud, consumer);
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }

    private Project createProject(Cloud cloud) {
        Project project = new Project();
        project.setId(idGenerator.getProjectId(cloud));
        project.setName(cloud.getName());
        project.setQuota(new Quota());
        //Setting now will cause Docker project to sync very often...
        project.setTimestamp(now().minusHours(1));
        return project;
    }
}
