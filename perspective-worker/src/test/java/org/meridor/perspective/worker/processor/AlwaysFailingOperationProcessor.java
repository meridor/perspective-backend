package org.meridor.perspective.worker.processor;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.meridor.perspective.worker.misc.impl.MockCloud;
import org.meridor.perspective.worker.operation.OperationProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AlwaysFailingOperationProcessor implements OperationProcessor, CloudConfigurationProvider {
    
    @Override
    public Cloud getCloud(String cloudId) {
        return new MockCloud();
    }

    @Override
    public Collection<Cloud> getClouds() {
        return Collections.singletonList(new MockCloud());
    }

    @Override
    public <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) throws Exception {
        throw new RuntimeException("Always failing");
    }

    @Override
    public <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) throws Exception {
        throw new RuntimeException("Always failing");
    }

    @Override
    public <I, O> Optional<O> process(Cloud cloud, OperationType operationType, Supplier<I> supplier) throws Exception {
        throw new RuntimeException("Always failing");
    }
}