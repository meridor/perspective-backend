package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.validator.annotation.*;
import org.meridor.perspective.shell.validator.annotation.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;
import static org.meridor.perspective.shell.validator.Entity.*;
import static org.meridor.perspective.shell.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class AddInstancesQuery implements Query<List<Instance>> {
    
    @Required
    private String name;
    
    @Required
    @ExistingEntity(PROJECT)
    @Filter(PROJECTS)
    private String project;
    
    @ExistingEntity(FLAVOR)
    @Filter(FLAVOR_NAMES)
    private String flavor;
    
    @Required
    @ExistingEntity(IMAGE)
    @Filter(IMAGE_NAMES)
    private String image;
    
    @ExistingEntity(NETWORK)
    @Filter(NETWORK_NAMES)
    private String network;
    
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

    public AddInstancesQuery withName(String name) {
        this.name = name;
        return this;
    }

    public AddInstancesQuery withProject(String project) {
        this.project = project;
        return this;
    }

    public AddInstancesQuery withFlavor(String flavor) {
        this.flavor = flavor;
        return this;
    }

    public AddInstancesQuery withImage(String image) {
        this.image = image;
        return this;
    }

    public AddInstancesQuery withNetwork(String network) {
        this.network = network;
        return this;
    }

    public AddInstancesQuery withRange(String range) {
        this.range = range;
        return this;
    }
    
    public AddInstancesQuery withCount(Integer count) {
        this.range = rangeFromCount(count);
        return this;
    }
    
    private String rangeFromCount(Integer count) {
        return String.format("1-%d", count);
    }
    
    public AddInstancesQuery withOptions(String options) {
        this.options = parseAssignment(options);
        return this;
    }

    @Override
    public List<Instance> getPayload() {
        List<Instance> instances = new ArrayList<>();
        Set<Integer> numbers = parseRange(range);
        if (numbers.size() > 1) {
            for (Integer i : numbers) {
                final String number = String.valueOf(i);
                String instanceName = (containsPlaceholder(name, Placeholder.NUMBER)) ?
                        replacePlaceholders(name, new HashMap<Placeholder, String>(){
                            {
                                put(Placeholder.NUMBER, number);
                            }
                        }) :
                        String.format("%s-%s", name, i);
                instances.add(createInstance(instanceName, project, flavor, image, network, options));
            }
        } else {
            instances.add(createInstance(name, project, flavor, image, network, options));
        }
        return instances;
    }
    
    private Instance createInstance(String name, String projectName, String flavorName, String imageName, String networkName, Map<String, Set<String>> options) {
        Instance instance = new Instance();
        instance.setName(name);

        List<Project> projects = projectsRepository.showProjects(queryProvider.get(ShowProjectsQuery.class).withNames(projectName));
        Project project = projects.get(0);
        instance.setProjectId(project.getId());
        instance.setCloudId(project.getCloudId());
        instance.setCloudType(project.getCloudType());
        
        if (flavorName != null) {
            List<Flavor> flavors = projectsRepository.showFlavors(
                    project.getName(),
                    project.getCloudType().name().toLowerCase(),
                    queryProvider.get(ShowFlavorsQuery.class).withNames(flavorName)
            );
            instance.setFlavor(flavors.get(0));
        }

        List<Image> images = imagesRepository.showImages(queryProvider.get(ShowImagesQuery.class).withNames(imageName));
        instance.setImage(images.get(0));
        
        if (networkName != null) {
            List<Network> networks = projectsRepository.showNetworks(
                    project.getName(),
                    project.getCloudType().name().toLowerCase(),
                    queryProvider.get(ShowNetworksQuery.class).withNames(networkName)
            );
            instance.setNetworks(networks);
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
