package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.query.QueryProvider;
import org.meridor.perspective.shell.query.ShowFlavorsQuery;
import org.meridor.perspective.shell.query.ShowNetworksQuery;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class ProjectsRepositoryImpl implements ProjectsRepository {
    
    @Autowired
    private ApiProvider apiProvider;
    
    @Autowired
    private QueryProvider queryProvider;
    
    @Autowired
    private SettingsAware settingsAware;
    
    private final List<Project> projectsCache = new ArrayList<>();
    
    @Override 
    public List<Project> showProjects(ShowProjectsQuery query) {
        if (projectsCache.isEmpty() || isProjectsCacheDisabled()) {
            GenericType<ArrayList<Project>> projectListType = new GenericType<ArrayList<Project>>() {};
            List<Project> allProjects = apiProvider.getProjectsApi().getAsXml(projectListType);
            projectsCache.addAll(allProjects);
        }
        return projectsCache.stream()
                .filter(query.getPayload())
                .sorted(
                        (p1, p2) -> Comparator.<String>naturalOrder().compare(p1.getName(), p2.getName())
                )
                .collect(Collectors.toList());
    }
    
    private boolean isProjectsCacheDisabled() {
        return settingsAware.hasSetting(Setting.DISABLE_PROJECTS_CACHE);
    }
    
    @Override 
    public Map<Project, List<Flavor>> showFlavors(ShowFlavorsQuery showFlavorsQuery) {
        List<Project> projects = showAllProjects(showFlavorsQuery.getProjects(), showFlavorsQuery.getClouds());
        return projects.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        p -> p.getFlavors().stream()
                                .filter(showFlavorsQuery.getPayload())
                                .sorted(
                                        (f1, f2) -> Comparator.<String>naturalOrder().compare(f1.getName(), f2.getName())
                                )
                                .collect(Collectors.toList())
                ));
    }
    
    @Override 
    public Map<Project, List<Network>> showNetworks(ShowNetworksQuery showNetworksQuery) {
        List<Project> projects = showAllProjects(showNetworksQuery.getProjects(), showNetworksQuery.getClouds());
        return projects.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        p -> p.getNetworks().stream()
                        .filter(showNetworksQuery.getPayload())
                        .sorted(
                                (n1, n2) -> Comparator.<String>naturalOrder().compare(n1.getName(), n2.getName())
                        )
                        .collect(Collectors.toList())
                ));
    }
    
    private List<Project> showAllProjects(String projectNames, String clouds) {
        return showProjects(queryProvider.get(ShowProjectsQuery.class).withNames(projectNames).withClouds(clouds));
    }
    
}
