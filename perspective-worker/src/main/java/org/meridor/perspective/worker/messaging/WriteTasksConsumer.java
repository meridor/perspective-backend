package org.meridor.perspective.worker.messaging;

import org.meridor.perspective.backend.messaging.Dispatcher;
import org.meridor.perspective.backend.messaging.impl.BaseConsumer;
import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.worker.Config;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.backend.messaging.MessageUtils.getRealQueueName;

@Component
public class WriteTasksConsumer extends BaseConsumer {

    private final Dispatcher dispatcher;
    
    private final WorkerMetadata workerMetadata;

    private final Config config;

    @Autowired
    public WriteTasksConsumer(WorkerMetadata workerMetadata, Dispatcher dispatcher, Config config) {
        this.workerMetadata = workerMetadata;
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    protected int getParallelConsumers() {
        return config.getWriteConsumers();
    }

    @Override
    protected Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    protected String getStorageKey() {
        return getRealQueueName(DestinationName.WRITE_TASKS.value(), workerMetadata.getCloudType());
    }

}
