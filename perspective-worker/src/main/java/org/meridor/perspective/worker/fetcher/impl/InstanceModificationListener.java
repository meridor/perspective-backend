package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.delayToLimit;

@Component
public class InstanceModificationListener extends LastModificationListener<Instance> {
    
    @Autowired
    private InstancesAware instancesAware;
    
    @Value("${perspective.fetch.delay.instances}")
    private int instancesFetchDelay;
    
    @PostConstruct
    public void init() {
        showInfo();
        instancesAware.addInstanceListener(this);
    }

    @Override
    protected int getLongTimeAgoLimit() {
        return delayToLimit(instancesFetchDelay);
    }

    @Override
    protected String getId(Instance instance) {
        return instance.getId();
    }

    @Override
    protected String getCloudId(Instance instance) {
        return instance.getCloudId();
    }

    @Override
    protected Instant getLastModifiedInstant(Instance instance) {
        return instance.getTimestamp().toInstant();
    }
}
