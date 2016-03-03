package org.meridor.perspective.shell.request;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.MetadataMap;
import org.meridor.perspective.shell.misc.DateUtils;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.result.FindInstancesResult;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.events.EventFactory.now;
import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.repository.impl.TextUtils.replacePlaceholders;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class AddImagesRequest implements Request<List<Image>> {

    @Required
    private String instanceNames;
    
    @Required
    private String imageName;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private QueryProvider queryProvider;
    
    @Autowired
    private DateUtils dateUtils;
    
    public AddImagesRequest withInstanceNames(String instanceNames) {
        this.instanceNames = instanceNames;
        return this;
    }
    
    public AddImagesRequest withName(String name) {
        this.imageName = name;
        return this;
    }

    @Override
    public List<Image> getPayload() {
        List<Image> images = new ArrayList<>();
        List<FindInstancesResult> matchingInstances = instancesRepository
                .findInstances(queryProvider.get(FindInstancesRequest.class).withNames(instanceNames));
        matchingInstances.forEach(i -> images.add(createImage(imageName, i)));
        return images;
    }
    
    private Image createImage(String imageName, FindInstancesResult instance) {
        Image image = new Image();
        String finalImageName = (imageName != null) ?
                replacePlaceholders(imageName, new HashMap<Placeholder, String>() {
                    {
                        put(Placeholder.NAME, instance.getName());
                        put(Placeholder.DATE, dateUtils.formatDate(now()));
                    }
                }) :
                String.format("%s-image", instance.getName());
        image.setName(finalImageName);
        image.setCloudId(instance.getCloudId());
        image.setCloudType(instance.getCloudType());
        List<String> projectIds = Collections.singletonList(instance.getProjectId());
        image.setProjectIds(projectIds);
        image.setCloudId(instance.getCloudId());
        image.setCloudType(instance.getCloudType());
        MetadataMap metadataMap = new MetadataMap();
        metadataMap.put(MetadataKey.INSTANCE_ID, instance.getRealId());
        image.setMetadata(metadataMap);
        return image;
    }

}
