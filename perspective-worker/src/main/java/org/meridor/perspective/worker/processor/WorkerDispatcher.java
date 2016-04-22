package org.meridor.perspective.worker.processor;

import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.events.InstanceEvent;
import org.meridor.perspective.events.ProjectEvent;
import org.meridor.perspective.framework.messaging.Dispatcher;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.messaging.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkerDispatcher implements Dispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerDispatcher.class);

    @Autowired
    private ProjectsProcessor projectsProcessor;

    @Autowired
    private InstancesProcessor instancesProcessor;
    
    @Autowired
    private ImagesProcessor imagesProcessor;

    @Override
    public Optional<Message> dispatch(Message message) {
        Object payload = message.getPayload();
        if (payload == null) {
            LOG.error("Can't dispatch message {} as its payload is null", message);
            return Optional.empty();
        }
        Optional<Processor> processorCandidate = getProcessor(payload);
        if (processorCandidate.isPresent()) {
            try {
                processorCandidate.get().process(message);
            } catch (Exception e) {
                return Optional.of(message);
            }
        } else {
            LOG.warn("Skipping message {} as no corresponding processor exists", message.getId());
        }
        return Optional.empty();
    }
    
    private Optional<Processor> getProcessor(Object payload) {
        if (payload instanceof InstanceEvent) {
            return Optional.of(instancesProcessor);
        } else if (payload instanceof ImageEvent) {
            return Optional.of(imagesProcessor);
        } else if (payload instanceof ProjectEvent) {
            return Optional.of(projectsProcessor);
        }
        return Optional.empty();
    }
}
