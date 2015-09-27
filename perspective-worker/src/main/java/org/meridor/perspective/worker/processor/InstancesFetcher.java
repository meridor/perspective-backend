package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.events.InstanceEvent;
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

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.beans.DestinationName.TASKS;
import static org.meridor.perspective.events.EventFactory.instanceToEvent;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;

@Component
public class InstancesFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesFetcher.class);

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @Destination(TASKS)
    private Producer producer;

    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private WorkerMetadata workerMetadata;

    @Async
    @Scheduled(fixedDelayString = "${perspective.fetch.delay.instances}")
    public void fetchInstances() {
        cloudConfigurationProvider.getClouds().forEach(this::fetchCloudInstances);
    }
    
    @IfNotLocked
    protected void fetchCloudInstances(Cloud cloud) {
        LOG.info("Fetching instances list for cloud = {}", cloud.getName());
        try {
            if (!operationProcessor.consume(cloud, OperationType.LIST_INSTANCES, getConsumer(cloud))) {
                throw new RuntimeException("Failed to get instances list from cloud = " + cloud.getName());
            }
        } catch (Exception e) {
            LOG.error("Error while fetching instances list for cloud = " + cloud.getName(), e);
        }
    }
    
    private Consumer<Set<Instance>> getConsumer(Cloud cloud) {
        return instances -> {
            CloudType cloudType = workerMetadata.getCloudType();
            for (Instance instance : instances) {
                instance.setCloudType(cloudType);
                instance.setCloudId(cloud.getId());
                InstanceEvent event = instanceToEvent(instance);
                event.setSync(true);
                producer.produce(message(cloudType, event));
            }
            LOG.debug("Saved {} fetched instances for cloud = {} to queue", instances.size(), cloud.getName());
        };
    }

}
