package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.OperationType;

import java.util.Optional;

public interface OperationsAware {

    Optional<Operation> getOperation(OperationType operationType);

}
