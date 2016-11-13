package org.meridor.perspective.worker.processor;

import org.meridor.perspective.common.events.impl.EventBusImpl;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkerEventBus extends EventBusImpl {
}
