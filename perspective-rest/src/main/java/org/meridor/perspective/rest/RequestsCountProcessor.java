package org.meridor.perspective.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.Processor;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestsCountProcessor {
        
        private static final Logger LOG = LoggerFactory.getLogger(RequestsCountProcessor.class);
        
        @Processor
        public void process(AtomicInteger atomicInteger) {
                LOG.info(String.format("Current value is %d", atomicInteger.get()));
                int newValue = atomicInteger.incrementAndGet();
                LOG.info(String.format("Incremented current value to %d", newValue));
        }
}
