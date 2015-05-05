package org.meridor.perspective.rest.aggregators;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.events.InstanceEvent;
import org.meridor.perspective.events.InstanceQueuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstancesAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesAggregator.class);

    public String getAggregationKey(InstanceEvent event) {
        return event.getUuid();
    }

    public void onInstanceQueuedEvent(Instance instance, InstanceQueuedEvent event) {
        instance.setId(event.getUuid());
        instance.setCreated(event.getTimestamp());
    }
}
