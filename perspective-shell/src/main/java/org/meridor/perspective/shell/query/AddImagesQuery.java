package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.repository.impl.TextUtils.replacePlaceholders;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class AddImagesQuery implements Query<List<Image>> {

    @Required
    private Set<String> instanceNames;
    
    @Required
    private String imageName;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    public AddImagesQuery withInstanceNames(String instanceNames) {
        this.instanceNames = parseEnumeration(instanceNames);
        return this;
    }
    
    public AddImagesQuery withName(String name) {
        this.imageName = name;
        return this;
    }

    @Override
    public List<Image> getPayload() {
        List<Image> images = new ArrayList<>();
        List<Instance> matchingInstances = instanceNames.stream().flatMap(n -> instancesRepository.showInstances(new ShowInstancesQuery().withNames(n)).stream()).collect(Collectors.toList());
        matchingInstances.forEach(i -> images.add(createImage(imageName, i)));
        return images;
    }
    
    private static Image createImage(String imageName, Instance instance) {
        Image image = new Image();
        String finalImageName = (imageName != null) ?
                replacePlaceholders(imageName, new HashMap<Placeholder, String>() {
                    {
                        put(Placeholder.NAME, instance.getName());
                    }
                }) :
                String.format("%s-image", instance.getName());
        image.setName(finalImageName);
        image.setCloudId(instance.getCloudId());
        image.setCloudType(instance.getCloudType());
        //TODO: complete this, use instance ID
        return image;
    }

}
