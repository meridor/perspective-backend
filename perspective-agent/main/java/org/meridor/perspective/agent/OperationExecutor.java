package org.meridor.perspective.agent;

import javax.ws.rs.core.Response;
import java.util.Optional;

public class OperationExecutor {
    
    private final OperationsRegistry operationsRegistry;

    public OperationExecutor(OperationsRegistry operationsRegistry) {
        this.operationsRegistry = operationsRegistry;
    }
    
    public <T> Response execute(OperationType operationType, T payload) {
        Optional<Operation<T>> operation = operationsRegistry.getOperation(operationType);
        if (!operation.isPresent()) {
            return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }
        return operation.get().apply(payload);
    }
}
