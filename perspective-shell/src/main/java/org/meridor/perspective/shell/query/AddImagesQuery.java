package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.MetadataMap;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.*;
import static org.meridor.perspective.events.EventFactory.now;
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
    
    @Autowired
    private QueryProvider queryProvider;
    
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
        List<Instance> matchingInstances = instanceNames.stream()
                .flatMap(n -> instancesRepository.showInstances(queryProvider.get(ShowInstancesQuery.class).withNames(n)).stream())
                .collect(Collectors.toList());
        matchingInstances.forEach(i -> images.add(createImage(imageName, i)));
        return images;
    }
    
    private static Image createImage(String imageName, Instance instance) {
        Image image = new Image();
        String finalImageName = (imageName != null) ?
                replacePlaceholders(imageName, new HashMap<Placeholder, String>() {
                    {
                        put(Placeholder.NAME, instance.getName());
                        put(Placeholder.DATE, getDate());
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
        if (instance.getMetadata() != null && instance.getMetadata().containsKey(MetadataKey.REGION)) {
            metadataMap.put(MetadataKey.REGION, instance.getMetadata().get(MetadataKey.REGION));
        }
        metadataMap.put(MetadataKey.INSTANCE_ID, instance.getRealId());
        image.setMetadata(metadataMap);
        return image;
    }
    
    private static String getDate() {
        return getDateTimeFormatter().format(now());
    }
    
    private static DateTimeFormatter getDateTimeFormatter() {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral("_")
                .appendValue(HOUR_OF_DAY, 2)
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendValue(SECOND_OF_MINUTE, 2)
                .toFormatter();
    }

}
