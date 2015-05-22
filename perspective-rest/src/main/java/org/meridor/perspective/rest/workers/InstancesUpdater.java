package org.meridor.perspective.rest.workers;

import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InstancesUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);
    
    @Autowired
    private OperationProcessor operationProcessor;
    
    @Autowired
    private Storage storage;
    
}
