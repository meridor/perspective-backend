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
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.joinLines;
import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;

@Component
public class ShowCommands extends BaseCommands {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @CliCommand(value = "show projects", help = "Show available projects")
    public void showProjects(
            @CliOption(key = "names", help = "Project name") String names,
            @CliOption(key = "clouds", help = "Cloud types") String clouds
    ) {
        Set<String> parsedNames = parseEnumeration(names); 
        Set<String> parsedClouds = parseEnumeration(clouds); 
        ShowProjectsQuery showProjectsQuery = new ShowProjectsQuery(parsedNames, parsedClouds);
        validateExecuteShowResult(
                showProjectsQuery,
                new String[]{"Id", "Name", "Cloud"},
                q -> {
                    List<Project> projects = projectsRepository.showProjects(q);
                    return projects.stream()
                            .map(p -> new String[]{p.getId(), p.getName(), p.getCloudType().value()})
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show flavors", help = "Show available flavors")
    public void showFlavors(
            @CliOption(key = "names", help = "Flavor name") String names,
            @CliOption(key = "projectNames", help = "Project name") String projectNames,
            @CliOption(key = "clouds", help = "Cloud type") String clouds
    ) {
        Set<String> parsedNames = parseEnumeration(names);
        Set<String> parsedProjectNames = parseEnumeration(projectNames);
        Set<String> parsedClouds = parseEnumeration(clouds);
        ShowFlavorsQuery showFlavorsQuery = new ShowFlavorsQuery(parsedNames, parsedProjectNames, parsedClouds);
        validateExecuteShowResult(
                showFlavorsQuery,
                new String[]{"Id", "Name", "VCPUs", "RAM", "Root disk", "Ephemeral disk"},
                q -> {
                    List<Flavor> flavors = projectsRepository.showFlavors(q);
                    return flavors.stream()
                            .map(f -> new String[]{
                                    f.getId(), f.getName(),
                                    String.valueOf(f.getVcpus()), String.valueOf(f.getRam()),
                                    String.valueOf(f.getRootDisk()), String.valueOf(f.getEphemeralDisk())
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show networks", help = "Show available networks")
    public void showNetworks(
            @CliOption(key = "names", help = "Network name") String names,
            @CliOption(key = "projectNamess", help = "Project name") String projectNames,
            @CliOption(key = "clouds", help = "Cloud type") String clouds
    ) {
        Set<String> parsedNames = parseEnumeration(names);
        Set<String> parsedProjectNames = parseEnumeration(projectNames);
        Set<String> parsedClouds = parseEnumeration(clouds);
        ShowNetworksQuery showNetworksQuery = new ShowNetworksQuery(parsedNames, parsedProjectNames, parsedClouds);
        validateExecuteShowResult(
                showNetworksQuery,
                new String[]{"Id", "Name", "Subnets", "State", "Is Shared"},
                q -> {
                    List<Network> networks = projectsRepository.showNetworks(q);
                    return networks.stream()
                            .map(n -> new String[]{
                                    n.getId(), n.getName(),
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
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ShowInstancesQuery showInstancesQuery = new ShowInstancesQuery(id, name, flavor, image, state, cloud);
        validateExecuteShowResult(
                showInstancesQuery,
                new String[]{"Id", "Name", "Image", "Flavor", "State", "Last modified"},
                q -> {
                    List<Instance> instances = instancesRepository.showInstances(q);
                    return instances.stream()
                            .map(n -> new String[]{
                                    n.getId(), n.getName(),
                                    n.getImage().getName(),
                                    n.getFlavor().getName(),
                                    n.getState().value(),
                                    n.getTimestamp().toString()
                            })
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
        ShowImagesQuery showImagesQuery = new ShowImagesQuery(id, name, cloud);
        validateExecuteShowResult(
                showImagesQuery,
                new String[]{"Id", "Name", "State", "Size", "Last modified"},
                q -> {
                    List<Image> images = imagesRepository.showImages(q);
                    return images.stream()
                            .map(n -> new String[]{
                                    n.getId(), n.getName(),
                                    n.getState().value(),
                                    String.valueOf(n.getSize()),
                                    n.getTimestamp().toString()
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
}
