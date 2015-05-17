package org.meridor.perspective.engine.impl;

import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.engine.OperationsAware;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationProcessorImpl implements OperationProcessor {
    
    private static final Logger LOG = LoggerFactory.getLogger(OperationProcessorImpl.class);
    
    @Autowired
    private OperationsAware operationsAware;
    
    @Override
    public boolean process(CloudType cloudType, OperationType operationType, Object dataContainer) throws Exception {
        if (dataContainer == null) {
            throw new IllegalArgumentException("Data container can't be null");
        }
        
        if (!operationsAware.isOperationSupported(cloudType, operationType)) {
            throw new UnsupportedOperationException(String.format("No operation %s defined for cloud %s", operationType, cloudType));
        }

        return operationsAware.act(cloudType, operationType, dataContainer);
    }
    
}
