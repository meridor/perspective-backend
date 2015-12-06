package org.meridor.perspective.framework.messaging;

import org.meridor.perspective.framework.messaging.impl.BaseConsumer;
import org.springframework.stereotype.Component;

@Component
public class TestConsumer extends BaseConsumer {

    public static final String STORAGE_KEY = "test";
    
    @Override
    protected String getStorageKey() {
        return STORAGE_KEY;
    }

    @Override
    protected int getParallelConsumers() {
        return 5;
    }
}
