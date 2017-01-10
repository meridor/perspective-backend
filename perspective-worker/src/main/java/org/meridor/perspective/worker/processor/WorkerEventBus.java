package org.meridor.perspective.worker.processor;

import org.meridor.perspective.common.events.AbstractEventBus;
import org.meridor.perspective.worker.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkerEventBus extends AbstractEventBus {

    private final Config config;

    @Autowired
    public WorkerEventBus(Config config) {
        this.config = config;
    }

    @Override
    protected int getParallelConsumers() {
        return config.getEventConsumers();
    }

    @PreDestroy
    public void onDestroy() {
        shutdown();
    }
    
}
