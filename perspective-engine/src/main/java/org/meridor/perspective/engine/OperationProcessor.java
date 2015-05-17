package org.meridor.perspective.engine;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

public interface OperationProcessor {
    
    boolean process(CloudType cloudType, OperationType operationType, Object dataContainer) throws Exception;
    
}
