package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProjectsRepository {
    
    @Autowired
    private ApiProvider apiProvider;
    
    public List<Project> listProjects() {
        GenericType<ArrayList<Project>> projectListType = new GenericType<ArrayList<Project>>() {};
        return apiProvider.getProjectsApi().getAsXml(projectListType);
    }

    public List<Flavor> listFlavors() {
        List<Project> projects = listProjects();
        return projects.stream()
                .flatMap(p -> p.getFlavors().stream())
                .collect(Collectors.toList());
    }
    
    public List<Network> listNetworks() {
        List<Project> projects = listProjects();
        return projects.stream()
                .flatMap(p -> p.getNetworks().stream())
                .collect(Collectors.toList());
    }
    
}
