package org.meridor.perspective.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.api.annotations.Processor;

import java.util.concurrent.atomic.AtomicInteger;

@Filter(instanceOf = Event.class)
public class RequestsCountProcessor {
        
        private static final Logger LOG = LoggerFactory.getLogger(RequestsCountProcessor.class);
        
        private AtomicInteger atomicInteger = new AtomicInteger(0);
        
        @Processor
        public void process(Event event) {
                LOG.info(String.format("Processing event with timestamp = %s", event.getKey()));
                LOG.info(String.format("Current value is %d", atomicInteger.get()));
                int newValue = atomicInteger.incrementAndGet();
                LOG.info(String.format("Incremented current value to %d", newValue));
        }
}
