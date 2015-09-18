package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.validator.Required;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;

public class AddImagesQuery implements Query<List<Image>> {
    
    private static final String PLACEHOLDER = "$name";
    
    @Required
    private String names;
    
    private String imageName;
    
    private InstancesRepository instancesRepository;
    
    public AddImagesQuery(String imageName, String names, InstancesRepository instancesRepository) {
        this.imageName = imageName;
        this.names = names;
        this.instancesRepository = instancesRepository;
    }

    @Override
    public List<Image> getPayload() {
        List<Image> images = new ArrayList<>();
        Set<String> tokens = parseEnumeration(names);
        List<Instance> matchingInstances = tokens.stream().flatMap(t -> {
            ShowInstancesQuery showInstancesQuery = new ShowInstancesQuery(t, t);
            return instancesRepository.showInstances(showInstancesQuery).stream();
        }).collect(Collectors.toList());
        matchingInstances.forEach(i -> images.add(createImage(imageName, i)));
        return images;
    }
    
    private static Image createImage(String imageName, Instance instance) {
        Image image = new Image();
        String finalImageName = (imageName != null) ? 
                imageName.replace(PLACEHOLDER, instance.getName()) :
                String.format("%s-image", instance.getName());
        image.setName(finalImageName);
        image.setCloudId(instance.getCloudId());
        image.setCloudType(instance.getCloudType());
        //TODO: complete this, use instance ID
        return image;
    }

}
