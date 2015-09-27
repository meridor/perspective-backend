package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenstackUtils {
    
    @Autowired
    private IdGenerator idGenerator;
    
    public String getProjectId(Cloud cloud, String region) {
        return idGenerator.generate(Project.class, getProjectName(cloud, region));
    }
    
    public String getProjectName(Cloud cloud, String region) {
        return String.format("%s - %s", cloud.getName(), region);
    }
    
    private OpenstackUtils() {
        
    }
    
}
