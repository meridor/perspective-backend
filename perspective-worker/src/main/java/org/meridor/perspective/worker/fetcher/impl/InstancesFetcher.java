package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.backend.messaging.Destination;
import org.meridor.perspective.backend.messaging.IfNotLocked;
import org.meridor.perspective.backend.messaging.Producer;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.InstanceEvent;
import org.meridor.perspective.worker.Config;
import org.meridor.perspective.worker.fetcher.LastModificationAware;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.backend.messaging.MessageUtils.message;
import static org.meridor.perspective.beans.DestinationName.READ_TASKS;
import static org.meridor.perspective.events.EventFactory.instanceToEvent;
import static org.meridor.perspective.worker.processor.event.EventUtils.requestProjectSync;

@Component
public class InstancesFetcher extends BaseFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesFetcher.class);

    @Destination(READ_TASKS)
    private Producer producer;

    private final OperationProcessor operationProcessor;

    private final WorkerMetadata workerMetadata;

    private final ApplicationContext applicationContext;

    private final ProjectsAware projectsAware;

    private final EventBus eventBus;

    private final Config config;

    @Autowired
    public InstancesFetcher(OperationProcessor operationProcessor, WorkerMetadata workerMetadata, ApplicationContext applicationContext, ProjectsAware projectsAware, EventBus eventBus, Config config) {
        this.operationProcessor = operationProcessor;
        this.workerMetadata = workerMetadata;
        this.applicationContext = applicationContext;
        this.projectsAware = projectsAware;
        this.eventBus = eventBus;
        this.config = config;
    }

    @IfNotLocked(lockName = "all")
    @Override
    public void fetch(Cloud cloud) {
        LOG.info("Fetching instances list for cloud = {}", cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_INSTANCES, getConsumer(cloud))) {
                throw new RuntimeException("Failed to get instances list from cloud = " + cloud.getName());
            }
        } catch (Exception e) {
            LOG.error("Error while fetching instances list for cloud = " + cloud.getName(), e);
        }
    }
    
    @IfNotLocked(lockName = "ids")
    @Override
    public void fetch(Cloud cloud, Set<String> ids) {
        LOG.info("Fetching instances with ids = {} for cloud = {}", ids, cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_INSTANCES, ids, getConsumer(cloud))) {
                throw new RuntimeException(String.format(
                        "Failed to get instances with ids = %s from cloud = %s",
                        ids,
                        cloud.getName()
                ));
            }
        } catch (Exception e) {
            LOG.error(String.format(
                    "Error while fetching instances with ids = %s for cloud = %s",
                    ids,
                    cloud.getName()
            ), e);
        }
    }

    @Override
    protected int getFullSyncDelay() {
        return config.getInstancesFetchDelay();
    }

    @Override
    protected LastModificationAware getLastModificationAware() {
        return applicationContext.getBean(InstanceModificationListener.class);
    }

    private Consumer<Set<Instance>> getConsumer(Cloud cloud) {
        return instances -> {
            CloudType cloudType = workerMetadata.getCloudType();
            Set<String> allProjectIds = new HashSet<>();
            for (Instance instance : instances) {
                instance.setCloudType(cloudType);
                instance.setCloudId(cloud.getId());
                String projectId = instance.getProjectId();
                if (projectId != null) {
                    allProjectIds.add(projectId);
                }
                InstanceEvent event = instanceToEvent(instance);
                event.setSync(true);
                producer.produce(message(cloudType, event));
            }
            allProjectIds.stream()
                    .filter(projectId -> !projectsAware.projectExists(projectId))
                    .forEach(projectId -> requestProjectSync(eventBus, cloud, projectId));
            LOG.debug("Saved {} fetched instances for cloud = {} to queue", instances.size(), cloud.getName());
        };
    }

}
