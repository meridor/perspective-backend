package org.meridor.perspective.shell.client;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectsRepository {
    
    @Autowired
    private ApiProvider apiProvider;
    
    public List<Project> listProjects(CloudType cloudType) {
        GenericType<ArrayList<Project>> projectListType = new GenericType<ArrayList<Project>>() {};
        return apiProvider.getProjectsApi().getAsXml(projectListType);
    } 
    
}
