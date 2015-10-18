package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface OperationProcessor {

    <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) throws Exception;

    <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> supplier) throws Exception;
    
    <I, O> Optional<O> process(Cloud cloud, OperationType operationType, Supplier<I> supplier) throws Exception;

}
