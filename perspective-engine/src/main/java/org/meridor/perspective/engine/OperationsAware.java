package org.meridor.perspective.engine;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface OperationsAware {

    boolean isOperationSupported(CloudType cloudType, OperationType operationType);
    
    <T> boolean consume(Cloud cloud, OperationType operationType, Consumer<T> consumer) throws Exception;
    
    <T> boolean supply(Cloud cloud, OperationType operationType, Supplier<T> consumer) throws Exception;
    
}
