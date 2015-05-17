package org.meridor.perspective.rest.workers;

import org.apache.camel.Body;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.InstancesDeletingEvent;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstancesUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);
    
    @Autowired
    private OperationProcessor operationProcessor;
    
    @Autowired
    private Storage storage;

    public void deleteInstances(@Body InstancesDeletingEvent instancesDeletingEvent) {
        CloudType cloudType = instancesDeletingEvent.getCloudType();
        List<Instance> instances = instancesDeletingEvent.getInstances();
        String instancesUuids = instances.stream().map(Instance::getId).collect(Collectors.joining(", "));
        try {
            LOG.info("Deleting instances {} in cloud {}", instancesUuids, cloudType);
            if (!operationProcessor.process(cloudType, OperationType.DELETE_INSTANCES, instances)) {
                throw new RuntimeException("Failed to delete instances from the cloud");
            }
            storage.deleteInstances(cloudType, instances);
        } catch (Exception e) {
            LOG.error("Failed to delete instances in cloud " + cloudType, e);
        }
    }
    
}
