package org.meridor.perspective.worker.misc.impl;

import org.meridor.perspective.worker.Config;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractWorkerMetadata implements WorkerMetadata {

    @Autowired
    private Config config;    
    
    @Override
    public String getId() {
        return config.getWorkerId();
    }

}
