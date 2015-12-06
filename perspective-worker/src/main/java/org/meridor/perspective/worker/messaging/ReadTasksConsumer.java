package org.meridor.perspective.worker.messaging;

import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.framework.messaging.impl.BaseConsumer;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.framework.messaging.MessageUtils.getRealQueueName;

@Component
public class ReadTasksConsumer extends BaseConsumer {

    @Value("${perspective.messaging.read.consumers}")
    private int parallelConsumers;

    @Autowired
    private WorkerMetadata workerMetadata;

    @Override
    protected int getParallelConsumers() {
        return parallelConsumers;
    }

    @Override
    protected String getStorageKey() {
        return getRealQueueName(DestinationName.READ_TASKS.value(), workerMetadata.getCloudType());
    }

}
