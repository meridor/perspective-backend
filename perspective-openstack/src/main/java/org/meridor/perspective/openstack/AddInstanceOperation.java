package org.meridor.perspective.openstack;

import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Network;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.meridor.perspective.beans.AvailabilityZone;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
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

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            String region = instance.getMetadata().get(MetadataKey.REGION);
            ServerApi serverApi = novaApi.getServerApi(region);
            String instanceName = instance.getName();
            String flavorId = instance.getFlavor().getId();
            String imageId = instance.getImage().getId();
            List<CreateServerOptions> optionsList = new ArrayList<>();
            
            Optional<Keypair> keypairCandidate = Optional.ofNullable(instance.getKeypair());
            if (keypairCandidate.isPresent()) {
                optionsList.add(CreateServerOptions.Builder.keyPairName(keypairCandidate.get().getName()));
            }
            
            if (!instance.getNetworks().isEmpty()) {
                List<Network> networks = instance.getNetworks().stream()
                        .map(n -> Network.builder().networkUuid(n.getId()).build())
                        .collect(Collectors.toList());
                optionsList.add(CreateServerOptions.Builder.novaNetworks(networks));
            }

            Optional<AvailabilityZone> availabilityZoneCandidate = Optional.ofNullable(instance.getAvailabilityZone());
            if (availabilityZoneCandidate.isPresent()) {
                optionsList.add(CreateServerOptions.Builder.availabilityZone(availabilityZoneCandidate.get().getName()));
            }
            
            CreateServerOptions[] options = optionsList.toArray(new CreateServerOptions[optionsList.size()]);
            ServerCreated createdServer = serverApi.create(instanceName, flavorId, imageId, options);
            String instanceId = createdServer.getId();
            instance.setRealId(instanceId);
            LOG.debug("Added instance {} ({})", instance.getName(), instance.getId());
            return instance;
        } catch (IOException e) {
            LOG.error("Failed to add instance " + instance.getName(), e);
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_INSTANCE};
    }
}
