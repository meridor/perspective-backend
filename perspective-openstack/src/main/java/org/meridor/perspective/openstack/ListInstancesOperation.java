package org.meridor.perspective.openstack;

import com.google.common.collect.FluentIterable;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Console;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.extensions.ConsolesApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);
    
    private static final String OFF = "off"; 

    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private ProjectsAware projects;
    
    @Autowired
    private ImagesAware images;
    
    @Value("${perspective.openstack.console.type:off}")
    private String consoleType;
    
    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            Integer overallInstancesCount = 0;
            for (String region : novaApi.getConfiguredRegions()) {
                Set<Instance> instances = new HashSet<>();
                try {
                    ServerApi serverApi = novaApi.getServerApi(region);
                    com.google.common.base.Optional<ConsolesApi> consolesApiCandidate = novaApi.getConsolesApi(region);
                    FluentIterable<Server> servers = serverApi.listInDetail().concat();
                    servers.forEach(s -> {
                        Instance instance = createInstance(cloud, region, s);
                        if (!consoleType.equals(OFF) && consolesApiCandidate.isPresent()) {
                            addConsoleUrl(instance, consolesApiCandidate.get());
                        }
                        instances.add(instance);
                    });
                    Integer regionInstancesCount = instances.size();
                    overallInstancesCount += regionInstancesCount;
                    LOG.debug("Fetched {} instances for cloud = {}, region = {}", regionInstancesCount, cloud.getName(), region);
                    consumer.accept(instances);
                } catch (Exception e) {
                    LOG.error("Failed to fetch instances for cloud = {}, region = {}", cloud.getName(), region);
                }
            }

            LOG.info("Fetched {} instances overall for cloud = {}", overallInstancesCount, cloud.getName());
            return true;
        } catch (IOException e) {
            LOG.error("Failed to fetch instances for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_INSTANCES};
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
        instance.setKeypair(keypair);
        
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
            List<Flavor> matchingFlavors = project.getFlavors().stream()
                    .filter(f -> f.getId().equals(server.getFlavor().getId()))
                    .collect(Collectors.toList());
            if (!matchingFlavors.isEmpty()) {
                instance.setFlavor(matchingFlavors.get(0));
            }
            
            //TODO: add information about network
        }
        
        String imageId = idGenerator.getImageId(cloud, server.getImage().getId());
        Optional<Image> imageCandidate = images.getImage(imageId);
        if (imageCandidate.isPresent()) {
            instance.setImage(imageCandidate.get());
        }
        
        return instance;
    }
    
    private void addConsoleUrl(Instance instance, ConsolesApi consolesApi) {
        try {
            Console console = consolesApi.getConsole(instance.getRealId(), Console.Type.fromValue(consoleType));
            URI url = console.getUrl();
            instance.getMetadata().put(MetadataKey.CONSOLE_URL, url.toString());
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
            case SOFT_DELETED:
            case DELETED:
                return InstanceState.DELETING;
            case UNRECOGNIZED:
            case UNKNOWN:
            case ERROR:
                return InstanceState.ERROR;
            case MIGRATING:
                return InstanceState.MIGRATING;
            case RESCUE:
            case SHELVED:
            case SHELVED_OFFLOADED:
            case SHUTOFF:
                return InstanceState.SHUTOFF;
            default:
            case ACTIVE:
                return InstanceState.LAUNCHED;
        }
    }

}
