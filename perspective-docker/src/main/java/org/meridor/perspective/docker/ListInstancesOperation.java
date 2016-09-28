package org.meridor.perspective.docker;

import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
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
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_INSTANCES;

@Component
public class ListInstancesOperation implements SupplyingOperation<Set<Instance>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListInstancesOperation.class);

    @Autowired
    private IdGenerator idGenerator;
    
    @Autowired
    private ApiProvider apiProvider;

    @Autowired
    private ProjectsAware projects;

    @Autowired
    private ImagesAware images;

    @Override
    public boolean perform(Cloud cloud, Consumer<Set<Instance>> consumer) {
        try {
            Api api = apiProvider.getApi(cloud);
            Set<Instance> instances = new HashSet<>();
            api.listContainers((
                    (cld, containerInfo) -> instances.add(createInstance(cld, containerInfo))
            ));
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
        LOG.warn("Not implemented. Doing full instances fetch instead.");
        return perform(cloud, consumer);
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_INSTANCES};
    }

    private Instance createInstance(Cloud cloud, ContainerInfo container) {
        Instance instance = new Instance();
        String realId = container.id();
        String instanceId = idGenerator.getInstanceId(cloud, realId);
        instance.setId(instanceId);
        instance.setRealId(realId);
        String containerName = trimLeadingSlashIfNeeded(container.name());
        instance.setName(containerName);
        instance.setCloudId(cloud.getId());
        instance.setCloudType(CloudType.DOCKER);

        String projectId = idGenerator.getProjectId(cloud);
        Optional<Project> projectCandidate = projects.getProject(projectId);
        if (projectCandidate.isPresent()) {
            instance.setProjectId(projectId);
        }
        
        String imageId = idGenerator.getImageId(cloud, container.image());
        Optional<Image> imageCandidate = images.getImage(imageId);
        if (imageCandidate.isPresent()) {
            instance.setImage(imageCandidate.get());
        }

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

    /*
        Docker inspect returns full container name = hostname/container_name, for localhost = /container_name.
        So far as we only work with localhost we can safely trim leading slash.
    */
    private String trimLeadingSlashIfNeeded(String rawName) {
        if (rawName != null && rawName.startsWith("/")) {
            return rawName.substring(1);
        }
        return rawName;
    }
    
    private static InstanceState createState(ContainerState state) {
        if (state.running()) {
            return InstanceState.LAUNCHED;
        } else if (state.oomKilled() || (state.error() != null && !state.error().isEmpty())) {
            return InstanceState.ERROR;
        } else if (state.paused()) {
            return InstanceState.PAUSED;
        } else if (state.restarting()) {
            return InstanceState.REBOOTING;
        } else {
            return InstanceState.SHUTOFF; 
        }
    }

}
