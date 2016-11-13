package org.meridor.perspective.rest.data.listeners;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.backend.storage.StorageEvent;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.meridor.perspective.rest.data.TableName.*;

@Component
public class ProjectsListener extends BaseEntityListener<Project> {

    @Autowired
    private ProjectsAware projectsAware;
    
    @PostConstruct
    public void init() {
        projectsAware.addProjectListener(this);
    }
    
    @Override
    public void onEvent(Project project, Project oldProject, StorageEvent event) {
        updateEntity(event, PROJECTS.getTableName(), project, oldProject);
        updateDerivedEntities(event, PROJECT_METADATA.getTableName(), project, oldProject, ProjectConverters::projectToMetadata);
        updateDerivedEntities(event, PROJECT_QUOTA.getTableName(), project, oldProject, ProjectConverters::projectToQuota);
        updateDerivedEntities(event, AVAILABILITY_ZONES.getTableName(), project, oldProject, ProjectConverters::projectToAvailabilityZones);
        updateDerivedEntities(event, CLOUDS.getTableName(), project, oldProject, ProjectConverters::projectToCloud);
        updateDerivedEntities(event, FLAVORS.getTableName(), project, oldProject, ProjectConverters::projectToFlavors);
        updateDerivedEntities(event, KEYPAIRS.getTableName(), project, oldProject, ProjectConverters::projectToKeypairs);
        updateDerivedEntities(event, NETWORKS.getTableName(), project, oldProject, ProjectConverters::projectToNetworks);
        updateDerivedEntities(event, NETWORK_SUBNETS.getTableName(), project, oldProject, ProjectConverters::projectToNetworkSubnets);
    }

}
