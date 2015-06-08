package org.meridor.perspective.rest.workers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.InstanceEvent;
import org.meridor.perspective.framework.CloudConfigurationProvider;
import org.meridor.perspective.rest.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.meridor.perspective.beans.DestinationName.INSTANCES;
import static org.meridor.perspective.events.EventFactory.instanceToEvent;

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
    
    @Autowired
    private FSMBuilderAware fsmBuilderAware;

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
                    InstanceEvent event = instanceToEvent(instance);
                    event.setSync(true);
                    producer.produce(event);
                }
                LOG.debug("Saved instances state for cloud {} to queue", t);
            } catch (Exception e) {
                LOG.error("Error while fetching instances list for cloud type " + t, e);
            }
        });
    }
    
    @Consume(queueName = INSTANCES, numConsumers = 5)
    public void processInstances(InstanceEvent event) {
        Instance instanceFromEvent = event.getInstance();
        CloudType cloudType = instanceFromEvent.getCloudType();
        Optional<Instance> instanceOrEmpty = storage.getInstance(cloudType, instanceFromEvent.getId());
        if (instanceOrEmpty.isPresent()) {
            Instance instance = instanceOrEmpty.get();
            InstanceEvent currentState = instanceToEvent(instance);
            Yatomata<InstanceFSM> fsm = fsmBuilderAware.get(InstanceFSM.class).build(currentState);
            event.setInstance(instance);
            LOG.debug(
                    "Updating instance {} from cloud {} from state = {} to state = {}",
                    instance.getId(),
                    cloudType,
                    currentState.getClass().getSimpleName(),
                    event.getClass().getSimpleName()
            );
            fsm.fire(event);
        } else if (event.isSync() && !storage.isInstanceDeleted(cloudType, instanceFromEvent.getId())) {
            LOG.debug(
                    "Syncing instance {} from cloud {} with state = {} for the first time",
                    event.getInstance().getId(),
                    cloudType,
                    event.getClass().getSimpleName()
            );
            Yatomata<InstanceFSM> fsm = fsmBuilderAware.get(InstanceFSM.class).build();
            fsm.fire(event);
        } else {
            LOG.debug(
                    "Will not update instance {} from cloud = {} as it does not exist or was already deleted",
                    instanceFromEvent.getId(),
                    cloudType
            );
        }
    }
    
}
