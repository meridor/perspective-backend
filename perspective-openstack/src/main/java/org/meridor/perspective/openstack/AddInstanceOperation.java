package org.meridor.perspective.openstack;

import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Network;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;

@Component
public class AddInstanceOperation implements ProcessingOperation<Instance, Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AddInstanceOperation.class);

    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @Autowired
    private ProjectsAware projectsAware;
    
    @Autowired
    private IdGenerator idGenerator;

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        String projectId = instance.getProjectId();
        Project project = projectsAware.getProject(projectId).get();
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            String region = project.getMetadata().get(MetadataKey.REGION);
            ServerApi serverApi = novaApi.getServerApi(region);
            String instanceName = instance.getName();
            String flavorId = instance.getFlavor().getId();
            String imageId = instance.getImage().getRealId();
            ServerCreated createdServer = serverApi.create(instanceName, imageId, flavorId, getServerOptions(instance));
            String realId = createdServer.getId();
            instance.getMetadata().put(MetadataKey.REGION, region);
            instance.setRealId(realId);
            String instanceId = idGenerator.getInstanceId(cloud, realId);
            instance.setId(instanceId);
            LOG.debug("Added instance {} ({})", instance.getName(), instance.getId());
            return instance;
        } catch (IOException e) {
            LOG.error("Failed to add instance " + instance.getName(), e);
            return null;
        }
    }
    
    private static CreateServerOptions getServerOptions(Instance instance) {
        CreateServerOptions serverOptions = new CreateServerOptions();
        Optional<Keypair> keypairCandidate = Optional.ofNullable(instance.getKeypair());
        if (keypairCandidate.isPresent()) {
            serverOptions.keyPairName(keypairCandidate.get().getName());
        }

        if (!instance.getNetworks().isEmpty()) {
            List<Network> networks = instance.getNetworks().stream()
                    .map(n -> Network.builder().networkUuid(n.getId()).build())
                    .collect(Collectors.toList());
            serverOptions.novaNetworks(networks);
        }

        Optional<AvailabilityZone> availabilityZoneCandidate = Optional.ofNullable(instance.getAvailabilityZone());
        if (availabilityZoneCandidate.isPresent()) {
            serverOptions.availabilityZone(availabilityZoneCandidate.get().getName());
        }
        
        return serverOptions;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_INSTANCE};
    }
}
