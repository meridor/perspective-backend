package org.meridor.perspective.rest.workers;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.engine.OperationProcessor;
import org.meridor.perspective.events.ProjectsSyncEvent;
import org.meridor.perspective.framework.CloudConfigurationProvider;
import org.meridor.perspective.rest.aspects.IfNotLocked;
import org.meridor.perspective.rest.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.meridor.perspective.events.EventFactory.projectsEvent;

@Component
public class ProjectsFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectsFetcher.class);
    
    @Produce(ref = "projects")
    private ProducerTemplate producer;
    
    @Autowired
    private OperationProcessor operationProcessor;
    
    @Autowired
    private Storage storage;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;

    @Scheduled(fixedDelay = 5000)
    @IfNotLocked
    public void fetchProjects() {
        cloudConfigurationProvider.getSupportedClouds().forEach(t -> {
            LOG.debug("Fetching projects list for cloud type {}", t);
            List<Project> projects = new ArrayList<>();
            try {
                if (!operationProcessor.process(t, OperationType.LIST_PROJECTS, projects)){
                    throw new RuntimeException("Failed to get projects list from the cloud");
                }
                producer.sendBody(projectsEvent(ProjectsSyncEvent.class, t, projects));
                LOG.debug("Saved projects for cloud type {} to queue", t);
            } catch (Exception e) {
                LOG.error("Error while fetching projects list for cloud " + t, e);
            }
        });
    }
    
    @Handler
    @IfNotLocked
    public void saveProjects(@Body ProjectsSyncEvent event) {
        CloudType cloudType = event.getCloudType();
        storage.saveProjects(cloudType, event.getProjects());
    }
    
}
