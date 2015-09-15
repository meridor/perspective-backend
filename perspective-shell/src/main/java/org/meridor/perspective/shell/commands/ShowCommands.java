package org.meridor.perspective.shell.commands;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.repository.query.ShowFlavorsQuery;
import org.meridor.perspective.shell.repository.query.ShowInstancesQuery;
import org.meridor.perspective.shell.repository.query.ShowNetworksQuery;
import org.meridor.perspective.shell.repository.query.ShowProjectsQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public class ShowCommands implements CommandMarker {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private TableRenderer tableRenderer;
    
    @CliCommand(value = "show projects", help = "Show available projects")
    public void showProjects(
            @CliOption(key = "name", help = "Project name") String name,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ShowProjectsQuery showProjectsQuery = new ShowProjectsQuery(name, cloud);
        Set<String> validationErrors = showProjectsQuery.validate();
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        }
        List<Project> projects = projectsRepository.showProjects(showProjectsQuery);
        List<String[]> projectData = projects.stream()
                .map(p -> new String[]{p.getId(), p.getName(), p.getCloudType().value()})
                .collect(Collectors.toList());
        String message = !projectData.isEmpty() ? 
                tableRenderer.render(new String[]{"Id", "Name", "Cloud"}, projectData) :
                nothingToShow();
        ok(message);
    }
    
    @CliCommand(value = "show flavors", help = "Show available flavors")
    public void showFlavors(
            @CliOption(key = "name", help = "Flavor name") String name,
            @CliOption(key = "projectName", help = "Project name") String projectName,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ShowFlavorsQuery showFlavorsQuery = new ShowFlavorsQuery(name, projectName, cloud);
        Set<String> validationErrors = showFlavorsQuery.validate();
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        }

        List<Flavor> flavors = projectsRepository.showFlavors(showFlavorsQuery);
        List<String[]> flavorData = flavors.stream()
                .map(f -> new String[]{
                        f.getId(), f.getName(),
                        String.valueOf(f.getVcpus()), String.valueOf(f.getRam()),
                        String.valueOf(f.getRootDisk()), String.valueOf(f.getEphemeralDisk())
                })
                .collect(Collectors.toList());
        String message = !flavorData.isEmpty() ?
                tableRenderer.render(new String[]{"Id", "Name", "VCPUs", "RAM", "Root disk", "Ephemeral disk"}, flavorData) :
                nothingToShow();
        ok(message);
    }
    
    @CliCommand(value = "show networks", help = "Show available networks")
    public void showNetworks(
            @CliOption(key = "name", help = "Network name") String name,
            @CliOption(key = "projectName", help = "Project name") String projectName,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {

        ShowNetworksQuery showNetworksQuery = new ShowNetworksQuery(name, projectName, cloud);
        Set<String> validationErrors = showNetworksQuery.validate();
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        }

        List<Network> networks = projectsRepository.showNetworks(showNetworksQuery);
        List<String[]> networkData = networks.stream()
                .map(n -> new String[]{
                        n.getId(), n.getName(),
                        n.getSubnets().stream().collect(Collectors.joining("\n")),
                        n.getState(), String.valueOf(n.isIsShared())
                })
                .collect(Collectors.toList());
        String message = !networkData.isEmpty() ? 
                tableRenderer.render(new String[]{"Id", "Name", "Subnets", "State", "Is Shared"}, networkData) :
                nothingToShow();
        ok(message);
    }
    
    @CliCommand(value = "show instances", help = "Show instances")
    public void showInstances(
            @CliOption(key = "name", help = "Network name") String name,
            @CliOption(key = "flavor", help = "Flavor name") String flavor,
            @CliOption(key = "image", help = "Image name") String image,
            @CliOption(key = "state", help = "Instance state") String state,
            @CliOption(key = "cloud", help = "Cloud type") String cloud

    ) {
        ShowInstancesQuery showInstancesQuery = new ShowInstancesQuery(name, flavor, image, state, cloud);
        Set<String> validationErrors = showInstancesQuery.validate();
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        }

        List<Instance> instances = instancesRepository.showInstances(showInstancesQuery);
        List<String[]> instancesData = instances.stream()
                .map(n -> new String[]{
                        n.getId(), n.getName(),
                        n.getImage().getName(),
                        n.getFlavor().getName(),
                        n.getState().value(),
                        n.getTimestamp().toString()
                })
                .collect(Collectors.toList());
        String message = !instancesData.isEmpty() ?
                tableRenderer.render(new String[]{"Id", "Name", "Image", "Flavor", "State", "Last modified"}, instancesData) :
                nothingToShow();
        ok(message);
    }
    
}
