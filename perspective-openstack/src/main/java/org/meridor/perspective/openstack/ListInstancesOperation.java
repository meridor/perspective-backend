package org.meridor.perspective.openstack;

import com.google.common.collect.FluentIterable;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);

    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @Autowired
    private IdGenerator idGenerator;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            Set<Instance> instances = new HashSet<>();
            for (String region : novaApi.getConfiguredRegions()) {
                try {
                    ServerApi serverApi = novaApi.getServerApi(region);
                    FluentIterable<Server> servers = serverApi.listInDetail().concat();
                    servers.forEach(s -> instances.add(createInstance(region, s)));
                    LOG.debug("Fetched instances for cloud = {}, region = {}", cloud.getName(), region);
                } catch (Exception e) {
                    LOG.error("Failed to fetch images for cloud = {}, region = {}", cloud.getName(), region);
                }
            }

            LOG.info("Fetched {} instances overall for cloud = {}", instances.size(), cloud.getName());
            consumer.accept(instances);
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

    private Instance createInstance(String region, Server server) {
        Instance instance = new Instance();
        String instanceId = idGenerator.generate(Instance.class, server.getId());
        instance.setId(instanceId);
        instance.setName(server.getName());
        instance.setState(stateFromStatus(server.getStatus()));
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
        metadata.put(MetadataKey.ID, server.getId());
        metadata.put(MetadataKey.REGION, region);
        instance.setMetadata(metadata);
        
        //TODO: add information about image and network
        return instance;
    }

    private static InstanceState stateFromStatus(Server.Status status) {
        switch (status) {
            case PASSWORD:
            case BUILD:
                return InstanceState.QUEUED;
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
