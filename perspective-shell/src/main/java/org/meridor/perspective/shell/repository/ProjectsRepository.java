package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.repository.query.ShowFlavorsQuery;
import org.meridor.perspective.shell.repository.query.ShowNetworksQuery;
import org.meridor.perspective.shell.repository.query.ShowProjectsQuery;
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
    
    public List<Project> showProjects(ShowProjectsQuery query) {
        GenericType<ArrayList<Project>> projectListType = new GenericType<ArrayList<Project>>() {};
        List<Project> allProjects = apiProvider.getProjectsApi().getAsXml(projectListType);
        return allProjects.stream().filter(query.getPayload()).collect(Collectors.toList());
    }
    
    public List<Flavor> showFlavors(ShowFlavorsQuery showFlavorsQuery) {
        ShowProjectsQuery showProjectsQuery = new ShowProjectsQuery(showFlavorsQuery.getProjectName(), showFlavorsQuery.getCloud());
        List<Project> projects = showProjects(showProjectsQuery);
        return projects.stream()
                .flatMap(p -> p.getFlavors().stream())
                .filter(showFlavorsQuery.getPayload())
                .collect(Collectors.toList());
    }
    
    public List<Network> showNetworks(ShowNetworksQuery showNetworksQuery) {
        ShowProjectsQuery showProjectsQuery = new ShowProjectsQuery(showNetworksQuery.getProjectName(), showNetworksQuery.getCloud());
        List<Project> projects = showProjects(showProjectsQuery);
        return projects.stream()
                .flatMap(p -> p.getNetworks().stream())
                .filter(showNetworksQuery.getPayload())
                .collect(Collectors.toList());
    }

}
