package org.meridor.perspective.worker.processor;

import org.meridor.perspective.events.ImageEvent;
import org.meridor.perspective.events.InstanceEvent;
import org.meridor.perspective.events.ProjectEvent;
import org.meridor.perspective.framework.messaging.Dispatcher;
import org.meridor.perspective.framework.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public void dispatch(Message message) {
        Object payload = message.getPayload();
        if (payload == null) {
            LOG.error("Can't dispatch message {} as its payload is null", message);
            return;
        }
        if (payload instanceof InstanceEvent) {
            instancesProcessor.process(message);
        } else if (payload instanceof ProjectEvent) {
            projectsProcessor.process(message);
        } else if (payload instanceof ImageEvent) {
            imagesProcessor.process(message);
        } else {
            LOG.warn("Skipping message {} as no corresponding processor exist", message.getId());
        }
    }
}
