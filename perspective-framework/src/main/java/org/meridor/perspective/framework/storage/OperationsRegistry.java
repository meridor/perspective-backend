package org.meridor.perspective.framework.storage;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

import java.util.Set;

public interface OperationsRegistry {

    Set<OperationType> getOperationTypes(CloudType cloudType);

    void addOperation(CloudType cloudType, OperationType operationType);

}
