package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class ProjectsRepository {
    
    @Autowired
    private ApiProvider apiProvider;
    
    public List<Project> listProjects(Optional<String> projectName, Optional<String> cloud) {
        GenericType<ArrayList<Project>> projectListType = new GenericType<ArrayList<Project>>() {};
        List<Project> allProjects = apiProvider.getProjectsApi().getAsXml(projectListType);
        return allProjects.stream().filter(getProjectPredicate(projectName, cloud)).collect(Collectors.toList());
    }
    
    private Predicate<Project> getProjectPredicate(Optional<String> projectName, Optional<String> cloud) {
        return project -> 
                ( !projectName.isPresent() || project.getName().contains(projectName.get()) ) &&
                ( !cloud.isPresent() || project.getCloudType().value().contains(cloud.get().toUpperCase()) );
    }

    public List<Flavor> listFlavors(
            Optional<String> name,
            Optional<String> projectName,
            Optional<String> cloud
    ) {
        List<Project> projects = listProjects(projectName, cloud);
        return projects.stream()
                .flatMap(p -> p.getFlavors().stream())
                .filter(getFlavorPredicate(name))
                .collect(Collectors.toList());
    }
    
    private Predicate<Flavor> getFlavorPredicate(Optional<String> name) {
        return flavor -> (!name.isPresent() || flavor.getName().contains(name.get()));
    }
    
    public List<Network> listNetworks(
            Optional<String> name,
            Optional<String> projectName,
            Optional<String> cloud
    ) {
        List<Project> projects = listProjects(projectName, cloud);
        return projects.stream()
                .flatMap(p -> p.getNetworks().stream())
                .filter(getNetworkPredicate(name))
                .collect(Collectors.toList());
    }

    private Predicate<Network> getNetworkPredicate(Optional<String> name) {
        return network -> (!name.isPresent() || network.getName().contains(name.get()));
    }
    
}
