package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface OperationProcessor {

    <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer);
    
    <T> boolean consume(Cloud cloud, OperationType operationType, Set<String> ids, Consumer<T> consumer);

    <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier);
    
    <I, O> Optional<O> process(Cloud cloud, OperationType operationType, Supplier<I> supplier);

}
