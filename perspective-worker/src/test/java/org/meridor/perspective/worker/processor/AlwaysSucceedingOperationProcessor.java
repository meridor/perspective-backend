package org.meridor.perspective.worker.processor;

import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.operation.OperationProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AlwaysSucceedingOperationProcessor implements OperationProcessor, CloudConfigurationProvider {
    
    @Override
    public Cloud getCloud(String cloudId) {
        return EntityGenerator.getCloud();
    }

    @Override
    public Collection<Cloud> getClouds() {
        return Collections.singletonList(EntityGenerator.getCloud());
    }

    @Override
    public <T> boolean consume(Cloud cloud, OperationType operationType, Set<String> ids, Consumer<T> consumer) {
        return true;
    }

    @Override
    public <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) {
        return true;
    }

    @Override
    public <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) {
        return true;
    }

    @Override
    public <I, O> Optional<O> process(Cloud cloud, OperationType operationType, Supplier<I> supplier) {
        @SuppressWarnings("unchecked")
        O ret = (O) supplier.get();
        return Optional.of(ret);
    }

}
