package org.meridor.perspective.shell.commands;

import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.request.*;
import org.meridor.perspective.shell.result.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.enumerateValues;

@Component
public class ShowCommands extends BaseCommands {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @Autowired
    private RequestProvider requestProvider;
    
    @CliCommand(value = "show projects", help = "Show available projects")
    public void showProjects(
            @CliOption(key = "id", help = "Project id") String id,
            @CliOption(key = "name", help = "Project name") String name,
            @CliOption(key = "cloud", help = "Cloud types") String cloud
    ) {
        FindProjectsRequest showProjectsQuery = requestProvider.get(FindProjectsRequest.class)
                .withIds(id)
                .withClouds(cloud)
                .withNames(name);
        validateExecuteShowResult(
                showProjectsQuery,
                new String[]{"Name", "Cloud"},
                q -> {
                    List<FindProjectsResult> projects = projectsRepository.findProjects(q);
                    return projects.stream()
                            .map(p -> new String[]{p.getName(), p.getCloudType().name().toLowerCase()})
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show flavors", help = "Show available flavors")
    public void showFlavors(
            @CliOption(key = "name", help = "Flavor name") String name,
            @CliOption(key = "project", help = "Project name") String project,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        FindFlavorsRequest showFlavorsQuery = requestProvider.get(FindFlavorsRequest.class)
                .withNames(name)
                .withProjects(project)
                .withClouds(cloud);
        validateExecuteShowResult(
                showFlavorsQuery,
                new String[]{"Name", "Project", "VCPUs", "RAM", "Root disk", "Ephemeral disk"},
                q -> {
                    List<FindFlavorsResult> flavors = projectsRepository.findFlavors(q);
                    return flavors.stream()
                            .map(f -> new String[]{
                                    f.getName(),
                                    f.getProjectName(),
                                    f.getVcpus(),
                                    f.getRam(),
                                    f.getRootDisk(),
                                    f.getEphemeralDisk()
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show networks", help = "Show available networks")
    public void showNetworks(
            @CliOption(key = "name", help = "Network name") String name,
            @CliOption(key = "project", help = "Project name") String project,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        FindNetworksRequest showNetworksQuery = requestProvider.get(FindNetworksRequest.class).withNames(name)
                .withProjects(project)
                .withClouds(cloud);
        validateExecuteShowResult(
                showNetworksQuery,
                new String[]{"Name", "Project", "Subnets", "State", "Is Shared"},
                q -> {
                    List<FindNetworksResult> networks = projectsRepository.findNetworks(q);
                    return networks.stream()
                            .map(n -> new String[]{
                                    n.getName(),
                                    n.getProjectName(),
                                    n.getSubnets().stream().collect(Collectors.joining("\n")),
                                    n.getState(),
                                    String.valueOf(n.isShared())
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show keypairs", help = "Show available keypairs")
    public void showKeypairs(
            @CliOption(key = "name", help = "Keypair name") String name,
            @CliOption(key = "project", help = "Project name") String project,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        FindKeypairsRequest showKeypairsQuery = requestProvider.get(FindKeypairsRequest.class).withNames(name)
                .withProjects(project)
                .withClouds(cloud);
        validateExecuteShowResult(
                showKeypairsQuery,
                new String[]{"Name", "Fingerprint", "Project"},
                q -> {
                    List<FindKeypairsResult> keypairs = projectsRepository.findKeypairs(q);
                    return keypairs.stream()
                            .map(k -> new String[]{k.getName(), k.getFingerprint(), k.getProjectName()})
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
            @CliOption(key = "project", help = "Project names") String project
    ) {
        FindInstancesRequest showInstancesQuery = requestProvider.get(FindInstancesRequest.class)
                .withIds(id)
                .withNames(name)
                .withFlavors(flavor)
                .withImages(image)
                .withStates(state)
                .withClouds(cloud)
                .withProjectNames(project);
        validateExecuteShowResult(
                showInstancesQuery,
                new String[]{"Name", "Project", "Image", "Flavor", "Network", "State", "Last modified"},
                q -> {
                    List<FindInstancesResult> instances = instancesRepository.findInstances(q);
                    return instances.stream()
                            .map(i -> new String[]{
                                    i.getName(),
                                    i.getProjectName(),
                                    i.getImageName(),
                                    i.getFlavorName(),
                                    i.getAddresses(),
                                    i.getState(),
                                    i.getLastUpdated()
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show images", help = "Show images")
    public void showImages(
            @CliOption(key = "id", help = "Image id") String id,
            @CliOption(key = "name", help = "Image name") String name,
            @CliOption(key = "state", help = "Image state") String state,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        FindImagesRequest showImagesQuery = requestProvider.get(FindImagesRequest.class)
                .withIds(id)
                .withNames(name)
                .withStates(state)
                .withClouds(cloud);
        validateExecuteShowResult(
                showImagesQuery,
                new String[]{"Name", "Projects", "State", "Last modified"},
                q -> {
                    List<FindImagesResult> images = imagesRepository.findImages(q);
                    return images.stream()
                            .map(i -> new String[] {
                                    i.getName(),
                                    enumerateValues(i.getProjectNames()),
                                    i.getState(),
                                    i.getLastUpdated()
                            })
                            .collect(Collectors.toList());
                }
        );
    }
    
    @CliCommand(value = "show console", help = "Show console for instances")
    public void showConsole(
            @CliOption(key = "", mandatory = true, help = "Comma separated instances names or patterns to match against instance name") String names
    ) {
        if (!Desktop.isDesktopSupported()) {
            warn("This operation is not supported on your platform.");
        }
        FindInstancesRequest showInstancesQuery = requestProvider.get(FindInstancesRequest.class)
                .withNames(names);
        validateExecuteShowResult(
                showInstancesQuery,
                q -> {
                    Map<String, Map<String, String>> instancesMetadata = instancesRepository.getInstancesMetadata(q);
                    if (instancesMetadata.isEmpty()) {
                        error(String.format("Instances not found: %s", names));
                    }
                    instancesMetadata.keySet().forEach(instanceName -> {
                        Map<String, String> metadata = instancesMetadata.get(instanceName);
                        String consoleUrlKey = MetadataKey.CONSOLE_URL.value();
                        if (!metadata.containsKey(consoleUrlKey)) {
                            warn(String.format("Matched instance \"%s\" but it didn't contain console information.", instanceName));
                            return;
                        }
                        String consoleUriString = metadata.get(consoleUrlKey);
                        try {
                            URI consoleUri = new URI(consoleUriString);
                            ok(String.format("Opening console for instance \"%s\"...", instanceName));
                            Desktop.getDesktop().browse(consoleUri);
                        } catch (URISyntaxException e) {
                            warn(String.format("Instance \"%s\" contains wrong console URL: %s.", instanceName, consoleUriString));
                        } catch (Exception e) {
                            error(String.format("Failed to open console for instance \"%s\". Either default browser is not set or it failed to open console at: %s", instanceName, consoleUriString));
                        }
                    });
                }
        );
    }

}
