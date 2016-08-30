package org.meridor.perspective.worker.operation.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class OperationProcessorImpl implements OperationProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(OperationProcessorImpl.class);
    
    @Autowired
    private OperationsAware operationsAware;

    @Override
    public <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) throws Exception {
        return consumeImpl(cloud, operationType, Collections.emptySet(), consumer);
    }

    @Override
    public <T> boolean consume(Cloud cloud, OperationType operationType, Set<String> ids, Consumer<T> consumer) throws Exception {
        return false;
    }

    private <T> boolean consumeImpl(Cloud cloud, OperationType operationType, Set<String> ids, Consumer<T> consumer) {
        Operation operation = doChecks(operationType, consumer);
        if (operation instanceof SupplyingOperation) {
            @SuppressWarnings("unchecked")
            SupplyingOperation<T> supplyingOperation = (SupplyingOperation<T>) operation;
            return ids.isEmpty() ? 
                    supplyingOperation.perform(cloud, consumer) :
                    supplyingOperation.perform(cloud, ids, consumer);
        } else {
            LOG.error("Operation {} should be a supplying operation", operationType);
            return false;
        }
    }

    @Override
    public <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) throws Exception {
        Operation operation = doChecks(operationType, supplier);
        if (operation instanceof ConsumingOperation) {
            @SuppressWarnings("unchecked")
            boolean result = ((ConsumingOperation<T>) operation).perform(cloud, supplier);
            return result;
        } else {
            LOG.error("Operation {} should be a consuming operation", operationType);
            return false;
        }

    }

    @Override
    public <I, O> Optional<O> process(Cloud cloud, OperationType operationType, Supplier<I> supplier) throws Exception {
        Operation operation = doChecks(operationType, supplier);
        if (operation instanceof ProcessingOperation) {
            @SuppressWarnings("unchecked")
            O result = ((ProcessingOperation<I, O>) operation).perform(cloud, supplier);
            return Optional.ofNullable(result);
        } else {
            LOG.error("Operation {} should be a processing operation", operationType);
            return Optional.empty();
        }
    }

    private Operation doChecks(OperationType operationType, Object action) {
        if (action == null) {
            throw new IllegalArgumentException("Action can't be null");
        }

        Optional<Operation> operationCandidate = operationsAware.getOperation(operationType);
        if (!operationCandidate.isPresent()) {
            throw new UnsupportedOperationException(String.format("No operation = %s defined", operationType));
        }
        return operationCandidate.get();
    }
}
