package org.meridor.perspective.rest.workers;

import org.apache.camel.Handler;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.rest.storage.IfNotLocked;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InstancesFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(InstancesFetcher.class);
    
    @Produce(ref = "instances")
    private ProducerTemplate producer;
    
    @Autowired
    private OperationProcessor operationProcessor;

    @Autowired
    private Storage storage;

    @Scheduled(fixedDelay = 5000)
    @IfNotLocked
    public void fetchProjects() {
        LOG.debug("Fetching instances list");
        List<Instance> instances = new ArrayList<>();
        try {
            operationProcessor.process(CloudType.MOCK, OperationType.LIST_INSTANCES, instances);
            producer.sendBody(instances);
            LOG.debug("Saved instances to queue");
        } catch (Exception e) {
            LOG.error("Error while fetching instances list", e);
        }
    }
    
    @Handler
    @IfNotLocked
    public void saveInstances(List<Instance> instances) {
        LOG.debug("Saving {} instances to storage", instances.size());
        storage.saveInstances(instances);
    }
    
}
