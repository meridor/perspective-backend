package org.meridor.perspective.shell.commands;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.shell.query.*;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.joinLines;

@Component
public class ShowCommands extends BaseCommands {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @Autowired
    private EntityFormatter entityFormatter;
    
    @Autowired
    private QueryProvider queryProvider;
    
    @CliCommand(value = "show projects", help = "Show available projects")
    public void showProjects(
            @CliOption(key = "name", help = "Project name") String name,
            @CliOption(key = "cloud", help = "Cloud types") String cloud
    ) {
        ShowProjectsQuery showProjectsQuery = getShowProjectsQuery(cloud, name);
        validateExecuteShowResult(
                showProjectsQuery,
                new String[]{"Name", "Cloud"},
                q -> {
                    List<Project> projects = projectsRepository.showProjects(q);
                    return projects.stream()
                            .map(p -> new String[]{p.getName(), p.getCloudType().value()})
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show flavors", help = "Show available flavors")
    public void showFlavors(
            @CliOption(key = "name", help = "Flavor name") String name,
            @CliOption(key = "projectName", help = "Project name") String projectName,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ShowFlavorsQuery showFlavorsQuery = queryProvider.get(ShowFlavorsQuery.class).withNames(name);
        validateExecuteShowResult(
                showFlavorsQuery,
                new String[]{"Name", "Project", "VCPUs", "RAM", "Root disk", "Ephemeral disk"},
                q -> {
                    Map<Project, List<Flavor>> flavorsMap = projectsRepository.showFlavors(projectName, cloud, q);
                    return flavorsMap.keySet().stream()
                            .flatMap(p -> {
                                List<Flavor> flavors = flavorsMap.get(p);
                                return flavors.stream().map(f -> new String[]{
                                        f.getName(), p.getName(),
                                        String.valueOf(f.getVcpus()), String.valueOf(f.getRam()),
                                        String.valueOf(f.getRootDisk()), String.valueOf(f.getEphemeralDisk())
                                });
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show networks", help = "Show available networks")
    public void showNetworks(
            @CliOption(key = "name", help = "Network name") String name,
            @CliOption(key = "projectName", help = "Project name") String projectName,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ShowNetworksQuery showNetworksQuery = queryProvider.get(ShowNetworksQuery.class).withNames(name);
        validateExecuteShowResult(
                showNetworksQuery,
                new String[]{"Name", "Project", "Subnets", "State", "Is Shared"},
                q -> {
                    Map<Project, List<Network>> networksMap = projectsRepository.showNetworks(projectName, cloud, q);
                    return networksMap.keySet().stream()
                            .flatMap(p -> {
                                List<Network> networks = networksMap.get(p);
                                return networks.stream().map(n -> new String[]{
                                        n.getName(), p.getName(),
                                        joinLines(new HashSet<>(n.getSubnets())),
                                        n.getState(), String.valueOf(n.isIsShared())
                                });
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show instances", help = "Show instances")
    public void showInstances(
            @CliOption(key = "id", help = "Instance id") String id,
            @CliOption(key = "name", help = "Instance name") String name,
            @CliOption(key = "flavor", help = "Flavor name") String flavor,
            @CliOption(key = "image", help = "Image name") String image,
            @CliOption(key = "state", help = "Instance state") String state,
            @CliOption(key = "cloud", help = "Cloud type") String cloud,
            @CliOption(key = "projects", help = "Project names") String projects
    ) {
        ShowInstancesQuery showInstancesQuery = queryProvider.get(ShowInstancesQuery.class)
                .withIds(id)
                .withNames(name)
                .withFlavors(flavor)
                .withImages(image)
                .withStates(state)
                .withClouds(cloud)
                .withProjectNames(projects);
        validateExecuteShowResult(
                showInstancesQuery,
                new String[]{"Name", "Project", "Image", "Flavor", "State", "Last modified"},
                q -> {
                    List<Instance> instances = instancesRepository.showInstances(q);
                    return entityFormatter.formatInstances(instances, cloud);
                }
        );
    }
    
    @CliCommand(value = "show images", help = "Show images")
    public void showImages(
            @CliOption(key = "id", help = "Image id") String id,
            @CliOption(key = "name", help = "Image name") String name,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ShowImagesQuery showImagesQuery = queryProvider.get(ShowImagesQuery.class)
                .withIds(id)
                .withNames(name)
                .withCloudNames(cloud);
        validateExecuteShowResult(
                showImagesQuery,
                new String[]{"Name", "Projects", "State", "Last modified"},
                q -> {
                    List<Image> images = imagesRepository.showImages(q);
                    return entityFormatter.formatImages(images);
                }
        );
    }
    
    private ShowProjectsQuery getShowProjectsQuery(String clouds, String projects) {
        return queryProvider.get(ShowProjectsQuery.class)
                .withClouds(clouds)
                .withNames(projects);
    }
}
