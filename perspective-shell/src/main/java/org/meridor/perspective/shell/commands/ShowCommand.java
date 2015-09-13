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
import org.springframework.stereotype.Component;

import java.util.List;
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
    public String showProjects() {
        List<Project> projects = projectsRepository.listProjects();
        List<String[]> projectData = projects.stream()
                .map(p -> new String[]{p.getId(), p.getName(), p.getCloudType().value()})
                .collect(Collectors.toList());
        return !projectData.isEmpty() ? 
                tableRenderer.render(new String[]{"Id", "Name", "Cloud"}, projectData) :
                nothingToShow();
    }
    
    @CliCommand(value = "show flavors", help = "Show available flavors")
    public String showFlavors() {
        List<Flavor> flavors = projectsRepository.listFlavors();
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
    public String showNetworks() {
        List<Network> networks = projectsRepository.listNetworks();
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
    public String showInstances() {
        List<Instance> instances = instancesRepository.listInstances();
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
