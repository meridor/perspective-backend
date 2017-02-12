package org.meridor.perspective.aws;

import com.amazonaws.services.ec2.model.InstanceStateName;
import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.*;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);

    private final IdGenerator idGenerator;

    private final ApiProvider apiProvider;

    private final ProjectsAware projectsAware;

    private final ImagesAware imagesAware;

    private final OperationUtils operationUtils;

    @Autowired
    public ListInstancesOperation(IdGenerator idGenerator, ApiProvider apiProvider, ProjectsAware projectsAware, ImagesAware imagesAware, OperationUtils operationUtils) {
        this.idGenerator = idGenerator;
        this.apiProvider = apiProvider;
        this.projectsAware = projectsAware;
        this.imagesAware = imagesAware;
        this.operationUtils = operationUtils;
    }

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try {
            apiProvider.forEachRegion(cloud, (region, api) -> {
                Set<Instance> instances = new HashSet<>();
                List<com.amazonaws.services.ec2.model.Instance> rawInstances = api.listInstances();
                rawInstances.forEach(ri -> instances.add(processInstance(cloud, region, ri)));
                LOG.debug("Fetched {} instances for cloud = {}, region = {}", instances.size(), cloud.getName(), region);
                consumer.accept(instances);
            });
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
                if (fetchMap.containsKey(region)) {
                    Set<String> instanceRealIds = fetchMap.get(region);
                    List<com.amazonaws.services.ec2.model.Instance> rawInstances = api.listInstances(instanceRealIds);
                    rawInstances.forEach(ri -> {
                        Instance instance = processInstance(cloud, region, ri);
                        consumer.accept(Collections.singleton(instance));
                        LOG.debug("Fetched instance {} ({}) for cloud = {}, region = {}", instance.getName(), instance.getId(), cloud.getName(), region);
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

    private Instance processInstance(Cloud cloud, String region, com.amazonaws.services.ec2.model.Instance rawInstance) {
        Instance instance = createInstance(cloud, region, rawInstance);
        addProject(cloud, region, instance);
        addImage(cloud, instance, rawInstance);
        addFlavor(cloud, region, instance, rawInstance);
        addAddresses(instance, rawInstance);
        addKeypairs(instance, rawInstance);
        return instance;
    }

    private Instance createInstance(Cloud cloud, String region, com.amazonaws.services.ec2.model.Instance rawInstance) {
        Instance instance = new Instance();
        String realId = String.valueOf(rawInstance.getInstanceId());
        String instanceId = idGenerator.getInstanceId(cloud, realId);
        instance.setId(instanceId);
        instance.setRealId(realId);
        instance.setName(rawInstance.getInstanceId()); //TODO: check this!
        instance.setFqdn(rawInstance.getPublicDnsName());
        instance.setCloudId(cloud.getId());
        instance.setCloudType(CloudType.AWS);

        ZonedDateTime created = ZonedDateTime.ofInstant(
                rawInstance.getLaunchTime().toInstant(),
                ZoneId.systemDefault()
        );
        instance.setCreated(created);
        InstanceStateName instanceStateName = InstanceStateName.fromValue(rawInstance.getState().getName());
        instance.setState(createState(instanceStateName));
        instance.setTimestamp(created);

        instance.setIsLocked(false);

        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, region);

        //TODO: console output for AWS is a screenshot!
//        metadata.put(MetadataKey.CONSOLE_URL, getConsoleUrl(instance.getId()));
        instance.setMetadata(metadata);

        return instance;
    }

    private static InstanceState createState(com.amazonaws.services.ec2.model.InstanceStateName instanceState) {
        switch (instanceState) {
            case Pending:
                return InstanceState.QUEUED;
            default:
            case Running:
                return InstanceState.LAUNCHED;
            case ShuttingDown:
                return InstanceState.SHUTTING_DOWN;
            case Terminated:
                return InstanceState.DELETING;
            case Stopping:
                return InstanceState.SUSPENDING;
            case Stopped:
                return InstanceState.SUSPENDED;
        }
    }

    private void addProject(Cloud cloud, String region, Instance instance) {
        String projectId = idGenerator.getProjectId(cloud, region);
        Optional<Project> projectCandidate = projectsAware.getProject(projectId);
        projectCandidate.ifPresent(project -> instance.setProjectId(projectId));
    }

    private void addImage(Cloud cloud, Instance instance, com.amazonaws.services.ec2.model.Instance rawInstance) {
        String realImageId = String.valueOf(rawInstance.getImageId());
        String imageId = idGenerator.getImageId(cloud, realImageId);
        Optional<Image> imageCandidate = imagesAware.getImage(imageId);
        imageCandidate.ifPresent(instance::setImage);
    }

    private void addFlavor(Cloud cloud, String region, Instance instance, com.amazonaws.services.ec2.model.Instance rawInstance) {
        String projectId = idGenerator.getProjectId(cloud, region);
        Optional<Project> projectCandidate = projectsAware.getProject(projectId);
        if (projectCandidate.isPresent()) {
            Project project = projectCandidate.get();
            String flavorId = rawInstance.getInstanceType();
            if (flavorId != null) {
                Optional<Flavor> matchingFlavor = project.getFlavors().stream()
                        .filter(f -> flavorId.equals(f.getId()))
                        .findFirst();
                matchingFlavor.ifPresent(instance::setFlavor);
            }
        }
    }

    private void addAddresses(Instance instance, com.amazonaws.services.ec2.model.Instance rawInstance) {
        List<String> ipAddresses = new ArrayList<>();
        if (rawInstance.getPublicIpAddress() != null) {
            ipAddresses.add(rawInstance.getPublicIpAddress());
        } else {
            rawInstance.getNetworkInterfaces().forEach(ni -> ni.getPrivateIpAddresses().forEach(
                    addr -> ipAddresses.add(String.format("%s (private)", addr))
            ));
        }
        instance.setAddresses(ipAddresses);
    }

    private void addKeypairs(Instance instance, com.amazonaws.services.ec2.model.Instance rawInstance) {
        if (rawInstance.getKeyName() != null) {
            Keypair keypair = new Keypair();
            keypair.setName(rawInstance.getKeyName());
            instance.setKeypairs(Collections.singletonList(keypair));
        }
    }

}
