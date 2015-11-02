package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.validator.annotation.*;
import org.meridor.perspective.shell.validator.annotation.Metadata;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.repository.impl.TextUtils.containsPlaceholder;
import static org.meridor.perspective.shell.repository.impl.TextUtils.replacePlaceholders;
import static org.meridor.perspective.shell.validator.Entity.*;
import static org.meridor.perspective.shell.validator.Field.*;
import static org.meridor.perspective.shell.validator.NumberRelation.GREATER_THAN;
import static org.meridor.perspective.shell.validator.NumberRelation.GREATER_THAN_EQUAL;
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
    @Filter(FLAVOR_IDS)
    private String flavor;
    
    @Required
    @ExistingEntity(IMAGE)
    @Filter(IMAGE_IDS)
    private String image;
    
    @ExistingEntity(NETWORK)
    @Filter(NETWORK_IDS)
    private String network;
    
    @RelativeToNumber(relation = GREATER_THAN, number = 0)
    private Integer from;

    @RelativeToNumericField(relation = GREATER_THAN_EQUAL, field = "from")
    private Integer to;
    
    @Metadata
    private String options;

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

    public AddInstancesQuery withTo(Integer to) {
        this.to = to;
        return this;
    }
    
    public AddInstancesQuery withCount(Integer count) {
        this.from = 1;
        this.to = count;
        return this;
    }

    public AddInstancesQuery withFrom(Integer from) {
        this.from = from;
        return this;
    }

    public AddInstancesQuery withOptions(String options) {
        this.options = options;
        return this;
    }

    @Override
    public List<Instance> getPayload() {
        List<Instance> instances = new ArrayList<>();
        if (from != null && to != null && to >= from ) {
            for (int i = from; i <= to; i++) {
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
    
    private static Instance createInstance(String name, String projectId, String flavorId, String imageId, String networkId, String options) {
        Instance instance = new Instance();
        instance.setName(name);
        instance.setProjectId(projectId);
        
        if (flavorId != null) {
            Flavor flavor = new Flavor();
            flavor.setId(flavorId);
            instance.setFlavor(flavor);
        }
        
        Image image = new Image();
        image.setId(imageId);
        instance.setImage(image);
        
        if (networkId != null) {
            Network network = new Network();
            network.setId(networkId);
            List<Network> networks = new ArrayList<>();
            networks.add(network);
            instance.setNetworks(networks);
        }

        Map<String, Set<String>> parsedOptions = TextUtils.parseAssignment(options);
        MetadataMap metadataMap = new MetadataMap();
        parsedOptions.keySet().forEach(
                k -> {
                    MetadataKey metadataKey = MetadataKey.fromValue(k);
                    Set<String> value = parsedOptions.get(k);
                    String metadataValue = TextUtils.enumerateValues(value);
                    metadataMap.put(metadataKey, metadataValue);
                }
        );
        instance.setMetadata(metadataMap);

        return instance;
    }

}
