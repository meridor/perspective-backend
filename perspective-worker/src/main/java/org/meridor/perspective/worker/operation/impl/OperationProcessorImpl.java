package org.meridor.perspective.worker.operation.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.OperationProcessor;
import org.meridor.perspective.worker.operation.OperationsAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class OperationProcessorImpl implements OperationProcessor {

    @Autowired
    private OperationsAware operationsAware;

    @Override
    public <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) throws Exception {
        doChecks(operationType, consumer);
        return operationsAware.consume(cloud, operationType, consumer);
    }

    @Override
    public <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) throws Exception {
        doChecks(operationType, supplier);
        return operationsAware.supply(cloud, operationType, supplier);
    }

    @Override
    public <I, O> Optional<O> process(Cloud cloud, OperationType operationType, Supplier<I> supplier) throws Exception {
        doChecks(operationType, supplier);
        return operationsAware.process(cloud, operationType, supplier);
    }

    private void doChecks(OperationType operationType, Object processor) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor can't be null");
        }

        if (!operationsAware.isOperationSupported(operationType)) {
            throw new UnsupportedOperationException(String.format("No operation = %s defined", operationType));
        }
    }
}
