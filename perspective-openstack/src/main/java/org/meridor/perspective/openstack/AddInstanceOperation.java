package org.meridor.perspective.openstack;

import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.AvailabilityZone;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.openstack4j.api.Builders;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;

@Component
public class AddInstanceOperation implements ProcessingOperation<Instance, Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AddInstanceOperation.class);

    private final ApiProvider apiProvider;

    private final ProjectsAware projectsAware;

    private final IdGenerator idGenerator;

    @Autowired
    public AddInstanceOperation(ApiProvider apiProvider, ProjectsAware projectsAware, IdGenerator idGenerator) {
        this.apiProvider = apiProvider;
        this.projectsAware = projectsAware;
        this.idGenerator = idGenerator;
    }

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        String projectId = instance.getProjectId();
        Project project = projectsAware.getProject(projectId).get();
        try {
            String region = project.getMetadata().get(MetadataKey.REGION);
            Api api = apiProvider.getApi(cloud, region);
            String realId = api.addInstance(getServerConfig(instance));
            instance.getMetadata().put(MetadataKey.REGION, region);
            instance.setRealId(realId);
            String instanceId = idGenerator.getInstanceId(cloud, realId);
            instance.setId(instanceId);
            LOG.debug("Added instance {} ({})", instance.getName(), instance.getId());
            return instance;
        } catch (Exception e) {
            LOG.error("Failed to add instance " + instance.getName(), e);
            return null;
        }
    }
    
    private static ServerCreate getServerConfig(Instance instance) {
        ServerCreateBuilder builder = Builders.server()
                .name(instance.getName())
                .flavor(instance.getFlavor().getId())
                .image(instance.getImage().getRealId());
        
        if (!instance.getKeypairs().isEmpty()) {
            builder = builder.keypairName(instance.getKeypairs().get(0).getName());
        }

        if (!instance.getNetworks().isEmpty()) {
            List<String> networks = instance.getNetworks().stream()
                    .map(org.meridor.perspective.beans.Network::getId)
                    .collect(Collectors.toList());
            builder = builder.networks(networks);
        }

        Optional<AvailabilityZone> availabilityZoneCandidate = Optional.ofNullable(instance.getAvailabilityZone());
        if (availabilityZoneCandidate.isPresent()) {
            builder = builder.availabilityZone(availabilityZoneCandidate.get().getName());
        }
        
        return builder.build();
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_INSTANCE};
    }
}
