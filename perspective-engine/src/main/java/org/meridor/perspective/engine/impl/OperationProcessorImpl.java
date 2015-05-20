package org.meridor.perspective.engine.impl;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.engine.OperationsAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class OperationProcessorImpl implements OperationProcessor {
    
    @Autowired
    private OperationsAware operationsAware;
    
    @Override
    public <T> boolean consume(CloudType cloudType, OperationType operationType, Consumer<T> consumer) throws Exception {
        doChecks(cloudType, operationType, consumer);
        return operationsAware.consume(cloudType, operationType, consumer);
    }

    @Override
    public <T> boolean supply(CloudType cloudType, OperationType operationType, Supplier<T> supplier) throws Exception {
        doChecks(cloudType, operationType, supplier);
        return operationsAware.supply(cloudType, operationType, supplier);
    }
    
    private void doChecks(CloudType cloudType, OperationType operationType, Object consumerOrProducer) {
        if (consumerOrProducer == null) {
            throw new IllegalArgumentException("Consumer or producer can't be null");
        }

        if (!operationsAware.isOperationSupported(cloudType, operationType)) {
            throw new UnsupportedOperationException(String.format("No operation %s defined for cloud %s", operationType, cloudType));
        }
    }
}
