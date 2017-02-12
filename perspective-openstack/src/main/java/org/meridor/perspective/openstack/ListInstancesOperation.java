package org.meridor.perspective.openstack;

import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.OperationUtils;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);
    
    private static final String OFF = "off";

    private static final String DOT = ".";

    private final ApiProvider apiProvider;

    private final IdGenerator idGenerator;

    private final ProjectsAware projects;

    private final ImagesAware imagesAware;

    private final OperationUtils operationUtils;
    
    @Value("${perspective.openstack.console.type:off}")
    private String consoleType;

    @Value("${perspective.openstack.default.domain:}") //Default is empty string
    private String defaultDomain;

    @Autowired
    public ListInstancesOperation(ApiProvider apiProvider, IdGenerator idGenerator, ProjectsAware projects, ImagesAware imagesAware, OperationUtils operationUtils) {
        this.apiProvider = apiProvider;
        this.idGenerator = idGenerator;
        this.projects = projects;
        this.imagesAware = imagesAware;
        this.operationUtils = operationUtils;
    }


    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try {
            final AtomicInteger overallInstancesCount = new AtomicInteger();
            apiProvider.forEachComputeRegion(cloud, (region, api) -> {
                Set<Instance> instances = new HashSet<>();
                try {
                    List<? extends org.openstack4j.model.compute.Server> servers = api.listInstances();
                    servers.forEach(s -> {
                        Instance instance = processInstance(cloud, region, s, api);
                        instances.add(instance);
                    });
                    Integer regionInstancesCount = instances.size();
                    overallInstancesCount.getAndUpdate(c -> c + regionInstancesCount);
                    LOG.debug("Fetched {} instances for cloud = {}, region = {}", regionInstancesCount, cloud.getName(), region);
                    consumer.accept(instances);
                } catch (Exception e) {
                    LOG.error(String.format("Failed to fetch instances for cloud = %s, region = %s", cloud.getName(), region), e);
                }
            });

            LOG.info("Fetched {} instances overall for cloud = {}", overallInstancesCount, cloud.getName());
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
            apiProvider.forEachComputeRegion(cloud, (region, api) -> {
                if (fetchMap.containsKey(region)) {
                    fetchMap.get(region).forEach(realId -> {
                        Optional<Server> instanceCandidate = api.getInstanceById(realId);
                        if (instanceCandidate.isPresent()) {
                            Server instance = instanceCandidate.get();
                            LOG.debug("Fetched instance {} ({}) for cloud = {}, region = {}", instance.getName(), instance.getId(), cloud.getName(), region);
                            consumer.accept(Collections.singleton(
                                    processInstance(cloud, region, instance, api)
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

    private Instance processInstance(Cloud cloud, String region, Server server, Api api) {
        Instance instance = createInstance(cloud, region, server);
        if (!consoleType.equals(OFF)) {
            addConsoleUrl(instance, api);
        }
        return instance;
    }
    
    private Instance createInstance(Cloud cloud, String region, Server server) {
        Instance instance = new Instance();
        String realId = server.getId();
        String instanceId = idGenerator.getInstanceId(cloud, realId);
        instance.setId(instanceId);
        instance.setRealId(realId);
        instance.setName(server.getName());
        instance.setState(stateFromStatus(server.getStatus()));
        instance.setCloudId(cloud.getId());
        instance.setCloudType(CloudType.OPENSTACK);
        
        Keypair keypair = new Keypair();
        keypair.setName(server.getKeyName());
        instance.setKeypairs(Collections.singletonList(keypair));
        
        ZonedDateTime created = ZonedDateTime.ofInstant(
                server.getCreated().toInstant(),
                ZoneId.systemDefault()
        );
        instance.setCreated(created);
        
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                server.getUpdated().toInstant(),
                ZoneId.systemDefault()
        );
        instance.setTimestamp(timestamp);
        
        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, region);
        instance.setMetadata(metadata);

        String projectId = idGenerator.getProjectId(cloud, region);
        Optional<Project> projectCandidate = getProject(projectId);
        if (projectCandidate.isPresent()) {
            instance.setProjectId(projectId);
            Project project = projectCandidate.get();
            List<Flavor> matchingFlavors = (server.getFlavor() != null) ?
                    project.getFlavors().stream()
                    .filter(f -> f.getId().equals(server.getFlavor().getId()))
                    .collect(Collectors.toList()) :
                    Collections.emptyList();
            if (!matchingFlavors.isEmpty()) {
                instance.setFlavor(matchingFlavors.get(0));
            }

            Map<String, List<? extends Address>> addresses = server.getAddresses().getAddresses();
            List<Network> networks = new ArrayList<>();
            addresses.keySet().forEach(networkName -> {
                List<String> ipAddresses = addresses.get(networkName).stream()
                        .map(Address::getAddr).collect(Collectors.toList());
                instance.setAddresses(ipAddresses);
                Optional<Network> networkCandidate = project.getNetworks().stream()
                        .filter(n -> n.getName().equals(networkName))
                        .findFirst();
                networkCandidate.ifPresent(networks::add);
            });
            instance.setNetworks(networks);
            String fqdn = getFQDN(server.getName());
            instance.setFqdn(fqdn);
        }
        
        if (server.getImage() != null) {
            String imageId = idGenerator.getImageId(cloud, server.getImage().getId());
            Optional<Image> imageCandidate = imagesAware.getImage(imageId);
            imageCandidate.ifPresent(instance::setImage);
        }
        
        return instance;
    }

    private String getFQDN(String instanceName) {
        if (instanceName.contains(DOT)) {
            return instanceName;
        }
        return defaultDomain.isEmpty() ?
                instanceName : String.format("%s.%s", instanceName, defaultDomain);
    }
    
    private void addConsoleUrl(Instance instance, Api api) {
        try {
            String consoleUrl = api.getInstanceConsoleUrl(instance.getRealId(), consoleType);
            instance.getMetadata().put(MetadataKey.CONSOLE_URL, consoleUrl);
        } catch (Exception e) {
            LOG.trace("Failed to fetch console information for instance {} ({})", instance.getName(), instance.getId());
        }
    }
    
    private Optional<Project> getProject(String projectId) {
        return projects.getProject(projectId);
    }
    
    private static InstanceState stateFromStatus(Server.Status status) {
        switch (status) {
            case PASSWORD:
            case BUILD:
                return InstanceState.LAUNCHING;
            case REBUILD:
                return InstanceState.REBUILDING;
            case SUSPENDED:
                return InstanceState.SUSPENDED;
            case PAUSED:
                return InstanceState.PAUSED;
            case VERIFY_RESIZE:
            case REVERT_RESIZE:
            case RESIZE:
                return InstanceState.RESIZING;
            case REBOOT:
                return InstanceState.REBOOTING;
            case HARD_REBOOT:
                return InstanceState.HARD_REBOOTING;
            case DELETED:
                return InstanceState.DELETING;
            case UNRECOGNIZED:
            case UNKNOWN:
            case ERROR:
                return InstanceState.ERROR;
            case MIGRATING:
                return InstanceState.MIGRATING;
            case STOPPED:
            case SHUTOFF:
                return InstanceState.SHUTOFF;
            default:
            case ACTIVE:
                return InstanceState.LAUNCHED;
        }
    }

}
