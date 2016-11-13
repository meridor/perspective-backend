package org.meridor.perspective.backend.messaging;

import org.meridor.perspective.backend.messaging.impl.BaseConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestConsumer extends BaseConsumer {

    public static final String STORAGE_KEY = "test";
    
    @Autowired
    private Dispatcher dispatcher;
    
    @Override
    protected String getStorageKey() {
        return STORAGE_KEY;
    }

    @Override
    protected int getParallelConsumers() {
        return 5;
    }

    @Override
    protected Dispatcher getDispatcher() {
        return dispatcher;
    }
}
