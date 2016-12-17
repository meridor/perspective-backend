package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.messaging.Dispatcher;
import org.meridor.perspective.backend.messaging.Message;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.worker.processor.event.MessageNotProcessedEvent;
import org.meridor.perspective.worker.processor.event.MessageProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class WorkerDispatcher implements Dispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerDispatcher.class);

    private final ApplicationContext applicationContext;
    
    private final Map<Class<?>, Processor> processorsMap = new HashMap<>();

    private final EventBus eventBus;
    
    @Autowired
    public WorkerDispatcher(ApplicationContext applicationContext, EventBus eventBus) {
        this.applicationContext = applicationContext;
        this.eventBus = eventBus;
    }

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
                eventBus.fireAsync(new MessageProcessedEvent(message));
            } catch (Exception e) {
                eventBus.fireAsync(new MessageNotProcessedEvent(message));
                return Optional.of(message);
            }
        } else {
            LOG.warn("Skipping message {} as no corresponding processor exists", message);
        }
        return Optional.empty();
    }
    
    private Optional<Processor> getProcessor(Object payload) {
        Class<?> payloadClass = payload.getClass();
        processorsMap.computeIfAbsent(payloadClass, pc -> {
            Optional<Processor> processorCandidate = applicationContext
                    .getBeansOfType(Processor.class).values().stream()
                    .filter(p -> p.isPayloadSupported(pc))
                    .findFirst();
            return processorCandidate.isPresent() ?
                    processorCandidate.get() : null;
        });
        return Optional.ofNullable(processorsMap.get(payloadClass));
    }
}
