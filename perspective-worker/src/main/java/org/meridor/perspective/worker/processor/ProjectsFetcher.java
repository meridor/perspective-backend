package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.ProjectSyncEvent;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.IfNotLocked;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.meridor.perspective.beans.DestinationName.TASKS;
import static org.meridor.perspective.events.EventFactory.projectEvent;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Component
public class ProjectsFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);

    @Destination(TASKS)
    private Producer producer;

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private WorkerMetadata workerMetadata;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @Async
    @Scheduled(fixedDelayString = "${perspective.fetch.delay.projects}")
    public void fetchProjects() {
        cloudConfigurationProvider.getClouds().forEach(this::fetchCloudProjects);
    }
    
    @IfNotLocked
    protected void fetchCloudProjects(Cloud cloud) {
        LOG.info("Fetching projects list for cloud {}", cloud.getName());
        try {
            Set<Project> projects = new HashSet<>();
            if (!operationProcessor.<Set<Project>>consume(cloud, OperationType.LIST_PROJECTS, projects::addAll)) {
                throw new RuntimeException("Failed to get projects list from the cloud");
            }
            CloudType cloudType = workerMetadata.getCloudType();
            for (Project project : projects) {
                project.setCloudId(cloud.getId());
                project.setCloudType(cloudType);
                ProjectSyncEvent event = projectEvent(ProjectSyncEvent.class, project);
                producer.produce(message(cloudType, event));
            }
            LOG.debug("Saved projects for cloud {} to queue", cloud.getName());
        } catch (Exception e) {
            LOG.error("Error while fetching projects list for cloud " + cloud.getName(), e);
        }
    }

}
