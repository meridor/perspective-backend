package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.ProjectSyncEvent;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.IfNotLocked;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.worker.fetcher.LastModificationAware;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.beans.DestinationName.READ_TASKS;
import static org.meridor.perspective.events.EventFactory.projectEvent;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Component
public class ProjectsFetcher extends BaseFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);

    @Destination(READ_TASKS)
    private Producer producer;

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private WorkerMetadata workerMetadata;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Value("${perspective.fetch.delay.projects}")
    private int fullSyncDelay;
    
    @IfNotLocked(lockName = "all")
    @Override
    public void fetch(Cloud cloud) {
        LOG.info("Fetching projects list for cloud = {}", cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_PROJECTS, getConsumer(cloud))) {
                throw new RuntimeException("Failed to get projects list from cloud = " + cloud.getName());
            }
        } catch (Exception e) {
            LOG.error("Error while fetching projects list for cloud = " + cloud.getName(), e);
        }
    }
    
    @IfNotLocked(lockName = "ids")
    @Override
    public void fetch(Cloud cloud, Set<String> ids) {
        LOG.info("Fetching projects with ids = {} for cloud = {}", ids, cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_PROJECTS, ids, getConsumer(cloud))) {
                throw new RuntimeException(String.format(
                        "Failed to get projects with ids = %s from cloud = %s",
                        ids,
                        cloud.getName()
                ));
            }
        } catch (Exception e) {
            LOG.error(String.format(
                    "Error while fetching projects with ids = %s for cloud = %s",
                    ids,
                    cloud.getName()
            ), e);
        }
    }

    @Override
    protected int getFullSyncDelay() {
        return fullSyncDelay;
    }

    @Override
    protected LastModificationAware getLastModificationAware() {
        return applicationContext.getBean(ProjectModificationListener.class);
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
