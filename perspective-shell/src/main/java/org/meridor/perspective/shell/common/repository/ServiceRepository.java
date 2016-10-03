package org.meridor.perspective.shell.common.repository;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

import java.util.Map;
import java.util.Set;

public interface ServiceRepository {
    
    Map<CloudType, Set<OperationType>> getSupportedOperations();
    
}
