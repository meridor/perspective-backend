package org.meridor.perspective.agent;

import java.util.Optional;

public interface OperationsRegistry {
    
    <T> Optional<Operation<T>> getOperation(OperationType operationType);
    
}
