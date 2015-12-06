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

import java.util.function.Consumer;

import static org.meridor.perspective.beans.DestinationName.READ_TASKS;
import static org.meridor.perspective.events.EventFactory.projectEvent;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Component
public class ProjectsFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);

    @Destination(READ_TASKS)
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
        LOG.info("Fetching projects list for cloud = {}", cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_PROJECTS, getConsumer(cloud))) {
                throw new RuntimeException("Failed to get projects list from cloud = " + cloud.getName());
            }
        } catch (Exception e) {
            LOG.error("Error while fetching projects list for cloud = " + cloud.getName(), e);
        }
    }
    
    private Consumer<Project> getConsumer(Cloud cloud) {
        return project -> {
            CloudType cloudType = workerMetadata.getCloudType();
            project.setCloudId(cloud.getId());
            project.setCloudType(cloudType);
            ProjectSyncEvent event = projectEvent(ProjectSyncEvent.class, project);
            producer.produce(message(cloudType, event));
            LOG.debug("Saved project {} for cloud = {} to queue", project.getName(), cloud.getName());
        };
    }

}
