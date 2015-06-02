package org.meridor.perspective.rest.workers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceStatus;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.meridor.perspective.beans.DestinationName.INSTANCES;
import static org.meridor.perspective.events.EventFactory.instanceEvent;
import static org.meridor.perspective.events.EventFactory.statusToEvent;

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
    public void fetchInstances() {
        cloudConfigurationProvider.getSupportedClouds().forEach(t -> {
            LOG.debug("Fetching instances list for cloud type {}", t);
            Set<Instance> instances = new HashSet<>();
            try {
                if (!operationProcessor.<Set<Instance>>consume(t, OperationType.LIST_INSTANCES, instances::addAll)) {
                    throw new RuntimeException("Failed to get instances list from the cloud");
                }
                for (Instance instance : instances) {
                    instance.setCloudType(t);
                    InstanceSyncEvent event = instanceEvent(InstanceSyncEvent.class, instance);
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
            syncInstance((InstanceSyncEvent) instanceEvent);
        } else {
            updateInstance(instanceEvent);
        }
    }
    
    private void updateInstance(InstanceEvent event) {
        Instance instanceFromEvent = event.getInstance();
        CloudType cloudType = instanceFromEvent.getCloudType();
        Optional<Instance> instance = storage.getInstance(cloudType, instanceFromEvent.getId());
        if (instance.isPresent()) {
            processEventIfNotExpired(instance.get(), event);
        } else {
            LOG.debug(
                    "Will not update instance with cloudType = {}, projectId = {}, id = {} as it does not exist",
                    cloudType,
                    instanceFromEvent.getProjectId(),
                    instanceFromEvent.getId()
            );
        }
    }
    
    private void processEventIfNotExpired(Instance instance, InstanceEvent event) {
        if (instance.getTimestamp().isBefore(event.getTimestamp())) {
            InstanceStatus status = instance.getStatus();
            InstanceEvent currentState = statusToEvent(status);
            Yatomata<InstanceFSM> fsm = new FSMBuilder<>(InstanceFSM.class).build(currentState);
            event.setInstance(instance);
            fsm.fire(event);
        } else {
            LOG.debug(
                    "Skipping event {} processing as respective instance {} was already updated after this event was created",
                    event,
                    instance
            );
        }
    }
    
    private void syncInstance(InstanceSyncEvent event) {
        LOG.debug("Processing instances sync event {}", event);
        Instance instance = event.getInstance();
        storage.saveInstance(instance);
    }
    
}
