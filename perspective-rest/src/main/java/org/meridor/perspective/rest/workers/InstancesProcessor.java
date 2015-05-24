package org.meridor.perspective.rest.workers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.InstanceEvent;
import org.meridor.perspective.events.InstanceSyncEvent;
import org.meridor.perspective.framework.CloudConfigurationProvider;
import org.meridor.perspective.rest.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.impl.FSMBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.meridor.perspective.beans.DestinationName.INSTANCES;
import static org.meridor.perspective.events.EventFactory.instancesEvent;

@Component
public class InstancesProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesProcessor.class);
    
    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private Storage storage;
    
    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;
    
    @Destination(INSTANCES)
    private Producer producer;

    @Scheduled(fixedDelayString = "${perspective.fetch.delay.instances}")
    @IfNotLocked
    public void fetchProjects() {
        cloudConfigurationProvider.getSupportedClouds().forEach(t -> {
            LOG.debug("Fetching instances list for cloud type {}", t);
            List<Instance> instances = new ArrayList<>();
            try {
                if (!operationProcessor.<List<Instance>>consume(t, OperationType.LIST_INSTANCES, instances::addAll)) {
                    throw new RuntimeException("Failed to get instances list from the cloud");
                }
                for (Instance instance : instances) {
                    InstanceSyncEvent event = instancesEvent(InstanceSyncEvent.class, t, instance);
                    producer.produce(event);
                }
                LOG.debug("Saved instances for cloud type {} to queue", t);
            } catch (Exception e) {
                LOG.error("Error while fetching instances list for cloud type " + t, e);
            }
        });
    }
    
    @Consume(queueName = INSTANCES, numConsumers = 5)
    public void processInstances(InstanceEvent instanceEvent) {
        if (instanceEvent instanceof InstanceSyncEvent) {
            syncInstances((InstanceSyncEvent) instanceEvent);
        } else {
            updateInstances(instanceEvent);
        }
    }
    
    private void updateInstances(InstanceEvent instanceEvent) {
        Yatomata<InstanceFSM> fsm = new FSMBuilder<>(InstanceFSM.class).build();
        fsm.fire(instanceEvent);
    }
    
    private void syncInstances(InstanceSyncEvent instanceSyncEvent) {
        CloudType cloudType = instanceSyncEvent.getCloudType();
        Instance instance = instanceSyncEvent.getInstance();
        LOG.debug("Saving instance {} to storage", instance);
        storage.saveInstance(cloudType, instance);
    }
    
}
