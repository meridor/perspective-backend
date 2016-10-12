package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.common.DropletStatus;
import com.myjeeva.digitalocean.pojo.Droplet;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private ApiProvider apiProvider;

    @Autowired
    private ProjectsAware projectsAware;

    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ImagesAware imagesAware;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try {
            Api api = apiProvider.getApi(cloud);
            Set<Instance> instances = new HashSet<>();
            List<Droplet> droplets = api.listDroplets();
            droplets.forEach(droplet -> instances.add(processInstance(cloud, droplet)));
            LOG.debug("Fetched {} instances for cloud = {}", instances.size(), cloud.getName());
            consumer.accept(instances);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to fetch instances for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Set<Instance>> consumer) {
        Set<Instance> instances = new HashSet<>();
        Set<String> realIds = getRealIds(ids);
        Api api = apiProvider.getApi(cloud);
        try {
            for (String realId : realIds) {
                Optional<Droplet> dropletCandidate = api.getDropletById(Integer.valueOf(realId));
                if (dropletCandidate.isPresent()) {
                    Instance instance = createInstance(cloud, dropletCandidate.get());
                    LOG.debug("Fetched instance {} ({}) for cloud = {}", instance.getName(), instance.getId(), cloud.getName());
                    consumer.accept(instances);
                }
            }
            return true;
        } catch (Exception e) {
            LOG.error(String.format(
                    "Failed to fetch instances with ids = %s for cloud = %s",
                    ids,
                    cloud.getName()
            ), e);
            return false;
        }
    }

    private Set<String> getRealIds(Set<String> ids) {
        Set<String> realIds = new HashSet<>();
        ids.forEach(id -> {
            Optional<Instance> instanceCandidate = instancesAware.getInstance(id);
            if (instanceCandidate.isPresent()) {
                realIds.add(instanceCandidate.get().getRealId());
            }
        });
        return realIds;
    }
    
    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_INSTANCES};
    }

    private Instance processInstance(Cloud cloud, Droplet droplet) {
        Instance instance = createInstance(cloud, droplet);
        addProject(cloud, instance, droplet);
        addImage(cloud, instance, droplet);
        addFlavor(cloud, instance, droplet);
        addAddresses(instance, droplet);
        addKeypairs(instance, droplet);
        //TODO: somehow save kernel info
        return instance;
    }
    
    private Instance createInstance(Cloud cloud, Droplet droplet) {
        Instance instance = new Instance();
        String realId = String.valueOf(droplet.getId());
        String instanceId = idGenerator.getInstanceId(cloud, realId);
        instance.setId(instanceId);
        instance.setRealId(realId);
        instance.setName(droplet.getName());
        instance.setCloudId(cloud.getId());
        instance.setCloudType(CloudType.DIGITAL_OCEAN);

        ZonedDateTime created = ZonedDateTime.ofInstant(
                droplet.getCreatedDate().toInstant(),
                ZoneId.systemDefault()
        );
        instance.setCreated(created);
        instance.setState(createState(droplet.getStatus()));
        droplet.getCreatedDate();
        instance.setTimestamp(created);
        
        instance.setIsLocked(droplet.isLocked());
        
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, droplet.getRegion().getName());
        instance.setMetadata(metadata);
        
        return instance;
    }

    private static InstanceState createState(DropletStatus dropletStatus) {
        switch (dropletStatus) {
            case NEW: return InstanceState.QUEUED;
            default:
            case ACTIVE: return InstanceState.LAUNCHED;
            case OFF:
            case ARCHIVE: return InstanceState.SHUTOFF;
        }
    }

    private void addProject(Cloud cloud, Instance instance, Droplet droplet) {
        String region = droplet.getRegion().getName();
        String projectId = idGenerator.getProjectId(cloud, region);
        Optional<Project> projectCandidate = projectsAware.getProject(projectId);
        if (projectCandidate.isPresent()) {
            instance.setProjectId(projectId);
        }
    }

    private void addImage(Cloud cloud, Instance instance, Droplet droplet) {
        String realImageId = String.valueOf(droplet.getImage().getId());
        String imageId = idGenerator.getImageId(cloud, realImageId);
        Optional<Image> imageCandidate = imagesAware.getImage(imageId);
        if (imageCandidate.isPresent()) {
            instance.setImage(imageCandidate.get());
        }
    }
    
    private void addFlavor(Cloud cloud, Instance instance, Droplet droplet) {
        //TODO: to be implemented. Should consider disk size, size and kernel..
    }

    private void addAddresses(Instance instance, Droplet droplet) {
        List<String> ipAddresses = new ArrayList<>();
        droplet.getNetworks().getVersion6Networks().forEach(n -> ipAddresses.add(n.getIpAddress()));
        droplet.getNetworks().getVersion4Networks().forEach(n -> ipAddresses.add(n.getIpAddress()));
        instance.setAddresses(ipAddresses);
    }

    private void addKeypairs(Instance instance, Droplet droplet) {
        List<Keypair> keypairs = droplet.getKeys().stream()
                .map(k -> {
                    Keypair keypair = new Keypair();
                    keypair.setName(k.getName());
                    return keypair;
                })
                .collect(Collectors.toList());
        instance.setKeypairs(keypairs);
    }
    
}
