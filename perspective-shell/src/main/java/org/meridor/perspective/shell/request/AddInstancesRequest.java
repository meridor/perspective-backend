package org.meridor.perspective.shell.request;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.result.FindImagesResult;
import org.meridor.perspective.shell.result.FindFlavorsResult;
import org.meridor.perspective.shell.result.FindNetworksResult;
import org.meridor.perspective.shell.result.FindProjectsResult;
import org.meridor.perspective.shell.validator.annotation.*;
import org.meridor.perspective.shell.validator.annotation.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;
import static org.meridor.perspective.shell.validator.Entity.*;
import static org.meridor.perspective.shell.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class AddInstancesRequest implements Request<List<Instance>> {
    
    @Required
    private String name;
    
    @Required
    @ExistingEntity(PROJECT)
    @Filter(PROJECTS)
    private String project;
    
    @ExistingEntity(value = FLAVOR, projectField = "project")
    @Filter(FLAVOR_NAMES)
    private String flavor;
    
    @Required
    @ExistingEntity(value = IMAGE, projectField = "project")
    @Filter(IMAGE_NAMES)
    private String image;
    
    @ExistingEntity(value = NETWORK, projectField = "project")
    @Filter(NETWORK_NAMES)
    private String network;
    
    @ExistingEntity(value = KEYPAIR, projectField = "project")
    @Filter(KEYPAIR_NAMES)
    private String keypair;
    
    @NumericRange
    private String range;
    
    @Metadata
    private Map<String, Set<String>> options;
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @Autowired
    private QueryProvider queryProvider;

    public AddInstancesRequest withName(String name) {
        this.name = name;
        return this;
    }

    public AddInstancesRequest withProject(String project) {
        this.project = project;
        return this;
    }

    public AddInstancesRequest withFlavor(String flavor) {
        this.flavor = flavor;
        return this;
    }

    public AddInstancesRequest withImage(String image) {
        this.image = image;
        return this;
    }

    public AddInstancesRequest withNetwork(String network) {
        this.network = network;
        return this;
    }

    public AddInstancesRequest withRange(String range) {
        this.range = range;
        return this;
    }
    
    public AddInstancesRequest withCount(Integer count) {
        this.range = rangeFromCount(count);
        return this;
    }
    
    public AddInstancesRequest withKeypair(String keypair) {
        this.keypair = keypair;
        return this;
    }
    
    private String rangeFromCount(Integer count) {
        return String.format("1-%d", count);
    }
    
    public AddInstancesRequest withOptions(String options) {
        this.options = parseAssignment(options);
        return this;
    }

    @Override
    public List<Instance> getPayload() {
        List<Instance> instances = new ArrayList<>();
        Set<Integer> numbers = parseRange(range);
        final int instancesCount = numbers.size();
        for (Integer i : numbers) {
            final String number = String.valueOf(i);
            String instanceName = (containsPlaceholder(name, Placeholder.NUMBER)) ?
                    replacePlaceholders(name, new HashMap<Placeholder, String>(){
                        {
                            put(Placeholder.NUMBER, number);
                        }
                    }) :
                    (instancesCount > 1) ?
                            String.format("%s-%s", name, i) : name;
            instances.add(createInstance(instanceName, project, flavor, image, network, keypair, options));
        }
        return instances;
    }
    
    private Instance createInstance(String name, String projectName, String flavorName, String imageName, String networkName, String keypairName, Map<String, Set<String>> options) {
        Instance instance = new Instance();
        instance.setName(name);

        List<FindProjectsResult> projects = projectsRepository.findProjects(queryProvider.get(FindProjectsRequest.class).withNames(projectName));
        FindProjectsResult project = projects.get(0);
        instance.setProjectId(project.getId());
        instance.setCloudId(project.getCloudId());
        instance.setCloudType(project.getCloudType());
        
        if (flavorName != null) {
            List<FindFlavorsResult> flavors = projectsRepository.findFlavors(
                    queryProvider.get(FindFlavorsRequest.class)
                            .withNames(flavorName)
                            .withProjects(project.getName())
                            .withClouds(project.getCloudType().name().toLowerCase())
            );
            instance.setFlavor(flavors.get(0).toFlavor());
        }

        List<FindImagesResult> images = imagesRepository.findImages(queryProvider.get(FindImagesRequest.class).withNames(imageName));
        instance.setImage(images.get(0).toImage());
        
        if (networkName != null) {
            List<FindNetworksResult> networkResults = projectsRepository.findNetworks(
                    queryProvider.get(FindNetworksRequest.class)
                            .withNames(networkName)
                            .withProjects(project.getName())
                            .withClouds(project.getCloudType().name().toLowerCase())
            );
            List<Network> networks = networkResults.stream()
                    .map(FindNetworksResult::toNetwork)
                    .collect(Collectors.toList());
            instance.setNetworks(networks);
        }
        
        if (keypairName !=  null) {
            Keypair keypair = new Keypair();
            keypair.setName(keypairName);
            instance.setKeypair(keypair);
        }

        MetadataMap metadataMap = new MetadataMap();
        options.keySet().forEach(
                k -> {
                    MetadataKey metadataKey = MetadataKey.fromValue(k);
                    Set<String> value = options.get(k);
                    String metadataValue = TextUtils.enumerateValues(value);
                    metadataMap.put(metadataKey, metadataValue);
                }
        );
        
        instance.setMetadata(metadataMap);

        return instance;
    }

}
