package org.meridor.perspective.engine.impl;

import org.meridor.perspective.config.Cloud;
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
    public <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) throws Exception {
        doChecks(cloud, operationType, consumer);
        return operationsAware.consume(cloud, operationType, consumer);
    }

    @Override
    public <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) throws Exception {
        doChecks(cloud, operationType, supplier);
        return operationsAware.supply(cloud, operationType, supplier);
    }
    
    private void doChecks(Cloud cloud, OperationType operationType, Object consumerOrProducer) {
        if (consumerOrProducer == null) {
            throw new IllegalArgumentException("Consumer or producer can't be null");
        }

        CloudType cloudType = cloud.getType();
        if (!operationsAware.isOperationSupported(cloudType, operationType)) {
            throw new UnsupportedOperationException(String.format("No operation %s defined for cloud %s", operationType, cloudType));
        }
    }
}
