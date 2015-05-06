package org.meridor.perspective.rest.fetchers;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.rest.locks.Locked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Locked
public class ProjectsFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);
    
    @Produce(ref = "projects")
    private ProducerTemplate producer;
    
    @Autowired
    private OperationProcessor operationProcessor;
    
    @Scheduled(fixedDelay = 5000)
    public void fetchProjects() {
        LOG.info("Fetching projects list...");
        List<Project> projects = new ArrayList<>();
        try {
            operationProcessor.process(CloudType.MOCK, OperationType.LIST_PROJECTS, projects);
            producer.sendBody(projects);
            LOG.info("Saved projects to queue");
        } catch (Exception e) {
            LOG.error("Error while fetching projects list", e);
        }
    }
    
}
