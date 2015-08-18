package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.framework.CloudConfigurationProvider;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.CloudType.OPENSTACK;
import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
@Operation(cloud = OPENSTACK, type = LIST_PROJECTS)
public class ListProjectsOperation {
    
    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;
    
    @EntryPoint
    public void listProjects(Cloud cloud, Consumer<Set<Project>> consumer) {
        //TODO: to be implemented!
    }
    
}
