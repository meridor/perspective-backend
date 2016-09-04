package org.meridor.perspective.worker.processor;

import org.meridor.perspective.framework.messaging.Dispatcher;
import org.meridor.perspective.framework.messaging.Message;
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

    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<Class<?>, Processor> processorsMap = new HashMap<>();

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
