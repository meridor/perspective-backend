package org.meridor.perspective.shell.commands;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.nothingToShow;

@Component
public class ShowCommand implements CommandMarker {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private TableRenderer tableRenderer;
    
    @CliCommand(value = "show projects", help = "Show available projects")
    public String showProjects(
            @CliOption(key = "name", help = "Project name") String name,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        List<Project> projects = projectsRepository.listProjects(
                Optional.ofNullable(name),
                Optional.ofNullable(cloud)
        );
        List<String[]> projectData = projects.stream()
                .map(p -> new String[]{p.getId(), p.getName(), p.getCloudType().value()})
                .collect(Collectors.toList());
        return !projectData.isEmpty() ? 
                tableRenderer.render(new String[]{"Id", "Name", "Cloud"}, projectData) :
                nothingToShow();
    }
    
    @CliCommand(value = "show flavors", help = "Show available flavors")
    public String showFlavors(
            @CliOption(key = "name", help = "Flavor name") String name,
            @CliOption(key = "projectName", help = "Project name") String projectName,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        List<Flavor> flavors = projectsRepository.listFlavors(
                Optional.ofNullable(name),
                Optional.ofNullable(cloud),
                Optional.ofNullable(projectName)
        );
        List<String[]> flavorData = flavors.stream()
                .map(f -> new String[]{
                        f.getId(), f.getName(),
                        String.valueOf(f.getVcpus()), String.valueOf(f.getRam()),
                        String.valueOf(f.getRootDisk()), String.valueOf(f.getEphemeralDisk())
                })
                .collect(Collectors.toList());
        return !flavorData.isEmpty() ?
                tableRenderer.render(new String[]{"Id", "Name", "VCPUs", "RAM", "Root disk", "Ephemeral disk"}, flavorData) :
                nothingToShow();
    }
    
    @CliCommand(value = "show networks", help = "Show available networks")
    public String showNetworks(
            @CliOption(key = "name", help = "Network name") String name,
            @CliOption(key = "projectName", help = "Project name") String projectName,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        List<Network> networks = projectsRepository.listNetworks(
                Optional.ofNullable(name),
                Optional.ofNullable(cloud),
                Optional.ofNullable(projectName)
        );
        List<String[]> networkData = networks.stream()
                .map(n -> new String[]{
                        n.getId(), n.getName(),
                        n.getSubnets().stream().collect(Collectors.joining("\n")),
                        n.getState(), String.valueOf(n.isIsShared())
                })
                .collect(Collectors.toList());
        return !networkData.isEmpty() ? 
                tableRenderer.render(new String[]{"Id", "Name", "Subnets", "State", "Is Shared"}, networkData) :
                nothingToShow();
    }
    
    @CliCommand(value = "show instances", help = "Show instances")
    public String showInstances(
            @CliOption(key = "name", help = "Network name") String name,
            @CliOption(key = "flavor", help = "Flavor name") String flavor,
            @CliOption(key = "image", help = "Image name") String image,
            @CliOption(key = "state", help = "Instance state") String state,
            @CliOption(key = "cloud", help = "Cloud type") String cloud

    ) {
        List<Instance> instances = instancesRepository.listInstances(
                Optional.ofNullable(name),
                Optional.ofNullable(flavor),
                Optional.ofNullable(image),
                Optional.ofNullable(state),
                Optional.ofNullable(cloud)
        );
        List<String[]> instancesData = instances.stream()
                .map(n -> new String[]{
                        n.getId(), n.getName(),
                        n.getImage().getName(),
                        n.getFlavor().getName(),
                        n.getState().value(),
                        n.getTimestamp().toString()
                })
                .collect(Collectors.toList());
        return !instancesData.isEmpty() ?
                tableRenderer.render(new String[]{"Id", "Name", "Image", "Flavor", "State", "Last modified"}, instancesData) :
                nothingToShow();
    }
    
}
