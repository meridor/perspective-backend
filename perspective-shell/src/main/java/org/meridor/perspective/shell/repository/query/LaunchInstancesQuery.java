package org.meridor.perspective.shell.repository.query;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.shell.repository.query.validator.Filter;
import org.meridor.perspective.shell.repository.query.validator.Positive;
import org.meridor.perspective.shell.repository.query.validator.Required;

import java.util.ArrayList;
import java.util.List;

import static org.meridor.perspective.shell.repository.query.validator.Field.*;

public class LaunchInstancesQuery extends BaseQuery<List<Instance>> {
    
    @Required
    private String name;
    
    @Filter(PROJECT)
    private String projectId;
    
    @Filter(FLAVOR)
    private String flavorId;
    
    @Filter(IMAGE)
    private String imageId;
    
    @Filter(NETWORK)
    private String networkId;
    
    @Positive
    private Integer count;
    
    private String options;

    public LaunchInstancesQuery(String name, String projectId, String flavorId, String imageId, String networkId, Integer count, String options) {
        this.name = name;
        this.projectId = projectId;
        this.flavorId = flavorId;
        this.imageId = imageId;
        this.networkId = networkId;
        this.count = count;
        this.options = options;
    }

    @Override
    public List<Instance> getPayload() {
        List<Instance> instances = new ArrayList<>();
        if (count > 1) {
            for (int i = 1; i <= count; i++) {
                String instanceName = String.format("%s-%s", name, i);
                instances.add(createInstance(instanceName, projectId, flavorId, imageId, networkId, options));
            }
        } else {
            instances.add(createInstance(name, projectId, flavorId, imageId, networkId, options));
        }
        return instances;
    }
    
    private static Instance createInstance(String name, String projectId, String flavorId, String imageId, String networkId, String options) {
        Instance instance = new Instance();
        instance.setName(name);
        instance.setProjectId(projectId);
        
        Flavor flavor = new Flavor();
        flavor.setId(flavorId);
        instance.setFlavor(flavor);
        
        Image image = new Image();
        image.setId(imageId);
        instance.setImage(image);
        
        Network network = new Network();
        network.setId(networkId);
        
        //TODO: use options!

        return instance;
    }

}
