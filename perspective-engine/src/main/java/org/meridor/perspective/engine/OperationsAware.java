package org.meridor.perspective.engine;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

public interface OperationsAware {

    boolean isOperationSupported(CloudType cloudType, OperationType operationType, Object dataContainer);
    
    void act(CloudType cloudType, OperationType operationType, Object dataContainer) throws Exception;
    
}
