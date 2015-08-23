package org.meridor.perspective.docker;

import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.Container;
import org.jclouds.docker.domain.ContainerSummary;
import org.jclouds.docker.features.ContainerApi;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
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
import java.util.UUID;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);

    @Autowired
    private DockerApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try (DockerApi dockerApi = apiProvider.getApi(cloud)) {
            Set<Instance> instances = new HashSet<>();
            ContainerApi containerApi = dockerApi.getContainerApi();
            for (ContainerSummary containerSummary : containerApi.listContainers()) {
                Container container = containerApi.inspectContainer(containerSummary.id());
                instances.add(createInstance(container));
            }

            LOG.debug("Fetched {} instances from Docker API", instances.size());
            consumer.accept(instances);
            return true;
        } catch (IOException e) {
            LOG.error("Failed to fetch instances", e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_INSTANCES};
    }

    private Instance createInstance(Container container) {
        Instance instance = new Instance();
        String id = UUID.fromString(container.id()).toString();
        instance.setId(id);
        instance.setName(container.name());
        ZonedDateTime created = ZonedDateTime.ofInstant(
                container.created().toInstant(),
                ZoneId.systemDefault()
        );
        instance.setCreated(created);
        instance.setState(stateFromStatus(container.status()));
        instance.setTimestamp(created);
        //TODO: add information about image and network
        return instance;
    }

    private static InstanceState stateFromStatus(String status) {
        switch (status) {
            case "running":
                return InstanceState.LAUNCHED;
            case "paused":
                return InstanceState.PAUSED;
            case "restarting":
                return InstanceState.REBOOTING;
            default:
            case "exited":
                return InstanceState.SHUTOFF;
        }
    }

}
