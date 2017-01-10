package org.meridor.perspective.worker.misc.impl;

import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractWorkerMetadata implements WorkerMetadata {

    @Value("${perspective.worker.id:worker}")
    private String id;

    @Override
    public String getId() {
        return id;
    }

}
