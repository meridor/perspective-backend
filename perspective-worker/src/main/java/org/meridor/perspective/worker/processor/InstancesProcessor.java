package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.events.InstanceEvent;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.messaging.Processor;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.Yatomata;

import java.util.Optional;

import static org.meridor.perspective.events.EventFactory.instanceToEvent;

@Component
public class InstancesProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesProcessor.class);

    @Autowired
    private InstancesAware storage;

    @Autowired
    private FSMBuilderAware fsmBuilderAware;

    @Override
    public void process(Message message) {
        LOG.trace("Processing message {}", message.getId());
        Optional<InstanceEvent> instanceEvent = message.getPayload(InstanceEvent.class);
        if (instanceEvent.isPresent()) {
            processInstances(instanceEvent.get());
        } else {
            LOG.error("Skipping empty message {}", message.getId());
        }
    }

    private void processInstances(InstanceEvent event) {
        Instance instanceFromEvent = event.getInstance();
        CloudType cloudType = instanceFromEvent.getCloudType();
        Optional<Instance> instanceOrEmpty = storage.getInstance(instanceFromEvent.getId());
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
        } else if (event.isSync() && !storage.isInstanceDeleted(instanceFromEvent.getId())) {
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
