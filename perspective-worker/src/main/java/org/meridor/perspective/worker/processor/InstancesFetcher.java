package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Instance;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

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

    @Scheduled(fixedDelayString = "${perspective.fetch.delay.instances}")
    @IfNotLocked
    public void fetchInstances() {
        cloudConfigurationProvider.getClouds().forEach(c -> {
            LOG.debug("Fetching instances list for cloud {}", c.getName());
            Set<Instance> instances = new HashSet<>();
            try {
                if (!operationProcessor.<Set<Instance>>consume(c, OperationType.LIST_INSTANCES, instances::addAll)) {
                    throw new RuntimeException("Failed to get instances list from the cloud");
                }
                CloudType cloudType = workerMetadata.getCloudType();
                for (Instance instance : instances) {
                    instance.setCloudType(cloudType);
                    instance.setCloudId(c.getId());
                    InstanceEvent event = instanceToEvent(instance);
                    event.setSync(true);
                    producer.produce(message(cloudType, event));
                }
                LOG.debug("Saved instances state for cloud {} to queue", c.getName());
            } catch (Exception e) {
                LOG.error("Error while fetching instances list for cloud " + c.getName(), e);
            }
        });
    }

}
