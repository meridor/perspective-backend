package org.meridor.perspective.worker.processor;

import org.meridor.perspective.common.events.AbstractEventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkerEventBus extends AbstractEventBus {

    @Value("${perspective.worker.event.consumers:5}")
    private int parallelConsumers;

    @Override
    protected int getParallelConsumers() {
        return parallelConsumers;
    }

    @PreDestroy
    public void onDestroy() {
        shutdown();
    }
    
}
