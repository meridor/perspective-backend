package org.meridor.perspective.openstack;

import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Supplier;

@Component
public class DeleteInstanceOperation implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteInstanceOperation.class);

    @Autowired
    private OpenstackApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            Instance instance = supplier.get();
            String region = instance.getMetadata().get(MetadataKey.REGION);
            ServerApi serverApi = novaApi.getServerApi(region);
            String instanceId = instance.getRealId();
            serverApi.delete(instanceId);
            LOG.debug("Deleted instance {} ({})", instance.getName(), instance.getId());
            return true;
        } catch (IOException e) {
            LOG.error("Failed to delete instance", e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{OperationType.DELETE_INSTANCE};
    }
}
