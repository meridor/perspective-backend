package org.meridor.perspective.engine.impl;

import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.engine.OperationsAware;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationProcessorImpl implements OperationProcessor {
    
    @Autowired
    private OperationsAware operationsAware;
    
    @Override
    public void process(CloudType cloudType, OperationType operationType, Object dataContainer) throws Exception {
        if (dataContainer == null) {
            throw new IllegalArgumentException("Data container can't be null");
        }
        
        if (!operationsAware.isOperationSupported(cloudType, operationType, dataContainer)) {
            throw new UnsupportedOperationException(String.format("No operation %s defined for cloud %s and parameter class %s", operationType, cloudType, dataContainer.getClass().getCanonicalName()));
        }
        
        operationsAware.act(cloudType, operationType, dataContainer);
    }
    
}
