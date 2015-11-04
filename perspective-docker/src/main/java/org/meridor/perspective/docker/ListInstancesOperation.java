package org.meridor.perspective.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.beans.MetadataMap;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private DockerApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try {
            DockerClient dockerApi = apiProvider.getApi(cloud);
            Set<Instance> instances = new HashSet<>();
            List<Container> containers = dockerApi.listContainers(DockerClient.ListContainersParam.allContainers());
            for (Container container : containers) {
                ContainerInfo containerInfo = dockerApi.inspectContainer(container.id());
                instances.add(createInstance(cloud, containerInfo));
            }

            LOG.debug("Fetched {} instances for cloud = {}", instances.size(), cloud.getName());
            consumer.accept(instances);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to fetch instances for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_INSTANCES};
    }

    private Instance createInstance(Cloud cloud, ContainerInfo container) {
        Instance instance = new Instance();
        String instanceId = idGenerator.getInstanceId(cloud, container.id());
        instance.setId(instanceId);
        instance.setRealId(container.id());
        instance.setName(container.name());
        ZonedDateTime created = ZonedDateTime.ofInstant(
                container.created().toInstant(),
                ZoneId.systemDefault()
        );
        instance.setCreated(created);
        instance.setState(createState(container.state()));
        instance.setTimestamp(created);
        MetadataMap metadata = new MetadataMap();
        instance.setMetadata(metadata);
        return instance;
    }

    private static InstanceState createState(ContainerState state) {
        if (state.running()) {
            return InstanceState.LAUNCHED;
        } else if (state.paused()) {
            return InstanceState.PAUSED;
        } else if (state.restarting()) {
            return InstanceState.REBOOTING;
        } else {
            return InstanceState.SHUTOFF; 
        }
    }

}
