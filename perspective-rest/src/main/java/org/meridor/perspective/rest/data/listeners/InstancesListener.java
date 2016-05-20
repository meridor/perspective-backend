package org.meridor.perspective.rest.data.listeners;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.StorageEvent;
import org.meridor.perspective.rest.data.converters.InstanceConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.meridor.perspective.rest.data.TableName.*;

@Component
public class InstancesListener extends BaseEntityListener<Instance> {

    @Autowired
    private InstancesAware instancesAware;

    @PostConstruct
    public void init() {
        instancesAware.addInstanceListener(this);
    }

    @Override
    public void onEvent(Instance instance, Instance oldInstance, StorageEvent event) {
        updateEntity(event, INSTANCES.getTableName(), instance, oldInstance);
        updateDerivedEntities(event, INSTANCE_METADATA.getTableName(), instance, oldInstance, InstanceConverters::instanceToMetadata);
        updateDerivedEntities(event, INSTANCE_NETWORKS.getTableName(), instance, oldInstance, InstanceConverters::instanceToNetworks);
    }

}
