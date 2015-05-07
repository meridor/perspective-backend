package org.meridor.perspective.rest.workers;

import org.apache.camel.Body;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
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

    public void deleteInstances(@Body List<Instance> instances) {
        try {
            String instancesUuids = instances.stream().map(Instance::getId).collect(Collectors.joining(", "));
            LOG.info("Deleting instances {}", instancesUuids);
            operationProcessor.process(CloudType.MOCK, OperationType.DELETE_INSTANCES, instances);
        } catch (Exception e) {
            LOG.error("Failed to delete instances");
        }
    }
    
}
