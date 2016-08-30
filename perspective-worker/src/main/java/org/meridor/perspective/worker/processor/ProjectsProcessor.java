package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.events.ProjectEvent;
import org.meridor.perspective.events.ProjectSyncEvent;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProjectsProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsProcessor.class);

    @Autowired
    private ProjectsAware storage;

    @Override
    public void process(Message message) {
        LOG.trace("Processing message {}", message.getId());
        Optional<ProjectEvent> projectEvent = message.getPayload(ProjectEvent.class);
        if (projectEvent.isPresent() && projectEvent.get() instanceof ProjectSyncEvent) {
            Project project = projectEvent.get().getProject();
            LOG.info("Syncing project state for project {} ({})", project.getName(), project.getId());
            storage.saveProject(project);
        } else {
            LOG.error("Skipping empty message {}", message.getId());
        }
    }
}
