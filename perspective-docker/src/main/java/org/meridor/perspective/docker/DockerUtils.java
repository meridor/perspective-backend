package org.meridor.perspective.docker;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DockerUtils {
    
    @Autowired
    private IdGenerator idGenerator;
    
    public String getProjectId(Cloud cloud) {
        return idGenerator.generate(Project.class, getProjectName(cloud));
    }
    
    public String getProjectName(Cloud cloud) {
        return cloud.getName();
    }
    
    public String getImageId(String realId) {
        return idGenerator.generate(Image.class, realId);
    }
    
    private DockerUtils() {
        
    }
    
}
