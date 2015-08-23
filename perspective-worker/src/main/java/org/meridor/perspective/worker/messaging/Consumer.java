package org.meridor.perspective.worker.messaging;

import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.framework.messaging.impl.BaseConsumer;
import org.meridor.perspective.worker.misc.WorkerMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.framework.messaging.MessageUtils.getRealQueueName;

@Component
public class Consumer extends BaseConsumer {

    @Autowired
    private WorkerMetadata workerMetadata;

    @Override
    protected String getStorageKey() {
        return getRealQueueName(DestinationName.TASKS.value(), workerMetadata.getCloudType());
    }

}
