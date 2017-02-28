package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.*;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.OperationUtils;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.cloud.compute.DiskConfiguration.Type.IMAGE;
import static com.google.cloud.compute.DiskConfiguration.Type.SNAPSHOT;
import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;
import static org.meridor.perspective.googlecloud.IdUtils.machineTypeIdToString;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);

    private final IdGenerator idGenerator;

    private final ApiProvider apiProvider;

    private final ProjectsAware projectsAware;

    private final ImagesAware imagesAware;

    private final OperationUtils operationUtils;

    private final DisksAware disksAware;

    @Autowired
    public ListInstancesOperation(IdGenerator idGenerator, ApiProvider apiProvider, ProjectsAware projectsAware, ImagesAware imagesAware, OperationUtils operationUtils, DisksAware disksAware) {
        this.idGenerator = idGenerator;
        this.apiProvider = apiProvider;
        this.projectsAware = projectsAware;
        this.imagesAware = imagesAware;
        this.operationUtils = operationUtils;
        this.disksAware = disksAware;
    }

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try {
            Set<Instance> instances = new HashSet<>();
            Api api = apiProvider.getApi(cloud);
            Map<ZoneId, RegionId> zoneToRegion = getZoneRegions(api);
            List<com.google.cloud.compute.Instance> rawInstances = api.listInstances();
            rawInstances.forEach(ri -> {
                String region = zoneToRegion.get(ri.getInstanceId().getZoneId()).getRegion();
                instances.add(processInstance(cloud, region, ri));
            });
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
        try {
            Map<String, Set<String>> fetchMap = operationUtils.getInstancesFetchMap(ids);
            apiProvider.forEachRegion(cloud, (region, api) -> {
                String regionName = region.getRegionId().getRegion();
                if (fetchMap.containsKey(regionName)) {
                    fetchMap.get(regionName).forEach(realId -> {
                        Optional<com.google.cloud.compute.Instance> instanceCandidate = api.getInstanceById(realId);
                        if (instanceCandidate.isPresent()) {
                            com.google.cloud.compute.Instance instance = instanceCandidate.get();
                            LOG.debug("Fetched instance {} ({}) for cloud = {}, region = {}", instance.getDescription(), instance.getInstanceId().getInstance(), cloud.getName(), region);
                            consumer.accept(Collections.singleton(
                                    processInstance(cloud, regionName, instance)
                            ));
                        }
                    });
                }
            });
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

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_INSTANCES};
    }

    private Map<ZoneId, RegionId> getZoneRegions(Api api) {
        Map<ZoneId, RegionId> ret = new HashMap<>();
        api.listRegions().forEach(r -> r.getZones().forEach(z -> ret.put(z, r.getRegionId())));
        return ret;
    }

    private Instance processInstance(Cloud cloud, String region, com.google.cloud.compute.Instance rawInstance) {
        Instance instance = createInstance(cloud, region, rawInstance);
        addProject(cloud, region, instance);
        addImage(cloud, instance, rawInstance);
        addFlavor(cloud, region, instance, rawInstance);
        addAddresses(instance, rawInstance);
        addKeypairs(instance, rawInstance);
        return instance;
    }

    private Instance createInstance(Cloud cloud, String region, com.google.cloud.compute.Instance rawInstance) {
        Instance instance = new Instance();
        String realId = String.valueOf(rawInstance.getInstanceId());
        rawInstance.getInstanceId().getZoneId();
        String instanceId = idGenerator.getInstanceId(cloud, realId);
        instance.setId(instanceId);
        instance.setRealId(realId);
        instance.setName(rawInstance.getDescription());
        //instance.setFqdn(); //TODO: can we determine it?
        instance.setCloudId(cloud.getId());
        instance.setCloudType(CloudType.AWS);

        ZonedDateTime created = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(rawInstance.getCreationTimestamp()),
                java.time.ZoneId.systemDefault()
        );
        instance.setCreated(created);

        InstanceInfo.Status status = rawInstance.getStatus();
        instance.setState(createState(status));
        instance.setTimestamp(created);

        instance.setIsLocked(false);

        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, region);
        instance.setMetadata(metadata);

        return instance;
    }

    private static InstanceState createState(InstanceInfo.Status instanceState) {
        switch (instanceState) {
            case PROVISIONING:
                return InstanceState.QUEUED;
            case STAGING:
                return InstanceState.LAUNCHING;
            default:
            case RUNNING:
                return InstanceState.LAUNCHED;
            case STOPPING:
                return InstanceState.SHUTTING_DOWN;
            case TERMINATED:
                return InstanceState.SHUTOFF;
        }
    }

    private void addProject(Cloud cloud, String region, Instance instance) {
        String projectId = idGenerator.getProjectId(cloud, region);
        Optional<Project> projectCandidate = projectsAware.getProject(projectId);
        projectCandidate.ifPresent(project -> instance.setProjectId(projectId));
    }

    private void addImage(Cloud cloud, Instance instance, com.google.cloud.compute.Instance rawInstance) {
        Collection<Disk> disks = disksAware.getDiskById(cloud, rawInstance.getInstanceId());
        disks.stream().filter(
                d -> {
                    DiskConfiguration.Type type = d.getConfiguration().getType();
                    return type == IMAGE || type == SNAPSHOT;
                }
        ).findFirst().ifPresent(disk -> {
            String realImageId = IdUtils.diskIdToString(disk.getDiskId());
            String imageId = idGenerator.getImageId(cloud, realImageId);
            imagesAware
                    .getImage(imageId)
                    .ifPresent(instance::setImage);
        });
    }

    private void addFlavor(Cloud cloud, String region, Instance instance, com.google.cloud.compute.Instance rawInstance) {
        String projectId = idGenerator.getProjectId(cloud, region);
        Optional<Project> projectCandidate = projectsAware.getProject(projectId);
        if (projectCandidate.isPresent()) {
            Project project = projectCandidate.get();
            String flavorId = machineTypeIdToString(rawInstance.getMachineType());
            if (flavorId != null) {
                Optional<Flavor> matchingFlavor = project.getFlavors().stream()
                        .filter(f -> flavorId.equals(f.getId()))
                        .findFirst();
                matchingFlavor.ifPresent(instance::setFlavor);
            }
        }
    }

    private void addAddresses(Instance instance, com.google.cloud.compute.Instance rawInstance) {
        List<String> ipAddresses = new ArrayList<>();
        rawInstance.getNetworkInterfaces().forEach(ni -> ipAddresses.add(ni.getNetworkIp()));
        instance.setAddresses(ipAddresses);
    }

    private void addKeypairs(Instance instance, com.google.cloud.compute.Instance rawInstance) {
        final String METADATA_KEY = "ssh-keys";
        Map<String, String> metadata = rawInstance.getMetadata().getValues();
        if (metadata.containsKey(METADATA_KEY)) {
            String rawKeys = metadata.get(METADATA_KEY);
            instance.setKeypairs(getSSHKeys(rawKeys));
        }
    }

    private List<Keypair> getSSHKeys(String rawKeys) {
        return Arrays.stream(rawKeys.split("\\n+"))
                .map(rk -> {
                    String[] pieces = rk.split("\\s+");
                    Keypair keypair = new Keypair();
                    keypair.setName("unknown");
                    if (pieces.length >= 3) {
                        String userName = String.valueOf(pieces[0]).replace(":ssa-rsa", "");
                        keypair.setName(userName);
                    }
                    return keypair;
                })
                .collect(Collectors.toList());
    }

}
