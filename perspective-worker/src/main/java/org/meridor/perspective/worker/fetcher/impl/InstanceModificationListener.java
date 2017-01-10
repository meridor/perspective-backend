package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.worker.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.delayToLimit;

@Component
public class InstanceModificationListener extends LastModificationListener<Instance> {

    private final InstancesAware instancesAware;

    private final Config config;
    
    @Autowired
    public InstanceModificationListener(InstancesAware instancesAware, Config config) {
        this.instancesAware = instancesAware;
        this.config = config;
    }

    @PostConstruct
    public void init() {
        showInfo();
        instancesAware.addInstanceListener(this);
    }

    @Override
    protected int getLongTimeAgoLimit() {
        return delayToLimit(config.getInstancesFetchDelay());
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
