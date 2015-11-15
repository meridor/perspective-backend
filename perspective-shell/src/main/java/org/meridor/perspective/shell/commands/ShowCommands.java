package org.meridor.perspective.shell.commands;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.shell.query.*;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.humanizedDuration;
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
    private QueryProvider queryProvider;
    
    @CliCommand(value = "show projects", help = "Show available projects")
    public void showProjects(
            @CliOption(key = "name", help = "Project name") String name,
            @CliOption(key = "cloud", help = "Cloud types") String cloud
    ) {
        ShowProjectsQuery showProjectsQuery = queryProvider.get(ShowProjectsQuery.class)
                .withNames(name)
                .withClouds(cloud);
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
                new String[]{"Name", "VCPUs", "RAM", "Root disk", "Ephemeral disk"},
                q -> {
                    List<Flavor> flavors = projectsRepository.showFlavors(projectName, cloud, q);
                    return flavors.stream()
                            .map(f -> new String[]{
                                    f.getName(),
                                    String.valueOf(f.getVcpus()), String.valueOf(f.getRam()),
                                    String.valueOf(f.getRootDisk()), String.valueOf(f.getEphemeralDisk())
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
                new String[]{"Name", "Subnets", "State", "Is Shared"},
                q -> {
                    List<Network> networks = projectsRepository.showNetworks(projectName, cloud, q);
                    return networks.stream()
                            .map(n -> new String[]{
                                    n.getName(),
                                    joinLines(new HashSet<>(n.getSubnets())),
                                    n.getState(), String.valueOf(n.isIsShared())
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
                new String[]{"Name", "Image", "Flavor", "State", "Last modified"},
                q -> {
                    List<Instance> instances = instancesRepository.showInstances(q);
                    return instances.stream()
                            .map(TextUtils::instanceToRow)
                            .collect(Collectors.toList());

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
                new String[]{"Name", "State", "Last modified"},
                q -> {
                    List<Image> images = imagesRepository.showImages(q);
                    return images.stream()
                            .map(i -> new String[]{
                                    i.getName(),
                                    i.getState().value(),
                                    humanizedDuration(i.getTimestamp())
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
}
