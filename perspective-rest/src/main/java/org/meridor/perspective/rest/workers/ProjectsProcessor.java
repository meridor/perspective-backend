package org.meridor.perspective.rest.workers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.ProjectSyncEvent;
import org.meridor.perspective.framework.CloudConfigurationProvider;
import org.meridor.perspective.rest.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.meridor.perspective.beans.DestinationName.PROJECTS;
import static org.meridor.perspective.events.EventFactory.projectEvent;

@Component
public class ProjectsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsProcessor.class);
    
    @Destination(PROJECTS)
    private Producer producer;
    
    @Autowired
    private OperationProcessor operationProcessor;
    
    @Autowired
    private Storage storage;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @Scheduled(fixedDelayString = "${perspective.fetch.delay.projects}")
    @IfNotLocked
    public void fetchProjects() {
        cloudConfigurationProvider.getCloudTypes().forEach(t -> {
            LOG.debug("Fetching projects list for cloud type {}", t);
            try {
                Set<Project> projects = new HashSet<>();
                if (!operationProcessor.<Set<Project>>consume(t, OperationType.LIST_PROJECTS, projects::addAll)) {
                    throw new RuntimeException("Failed to get projects list from the cloud");
                }
                for (Project project : projects) {
                    project.setCloudType(t);
                    ProjectSyncEvent event = projectEvent(ProjectSyncEvent.class, project);
                    producer.produce(event);
                }
                LOG.debug("Saved projects for cloud type {} to queue", t);
            } catch (Exception e) {
                LOG.error("Error while fetching projects list for cloud " + t, e);
            }
        });
    }
    
    @Consume(queueName = PROJECTS, numConsumers = 2)
    public void syncProjects(ProjectSyncEvent event) {
        storage.saveProject(event.getProject());
    }
    
}
