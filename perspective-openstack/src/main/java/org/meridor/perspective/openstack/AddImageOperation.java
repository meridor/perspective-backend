package org.meridor.perspective.openstack;

import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_IMAGE;

@Component
public class AddImageOperation implements ProcessingOperation<Image, Image> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AddImageOperation.class);
    
    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @Override
    public Image perform(Cloud cloud, Supplier<Image> supplier) {
        Image image = supplier.get();
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            String region = image.getMetadata().get(MetadataKey.REGION);
            ServerApi serverApi = novaApi.getServerApi(region);
            String instanceId = image.getMetadata().get(MetadataKey.INSTANCE_ID);
            String imageId = serverApi.createImageFromServer(image.getName(), instanceId);
            image.getMetadata().put(MetadataKey.ID, imageId);
            LOG.debug("Added image {} ({})", image.getName(), image.getId());
            return image;
        } catch (IOException e) {
            LOG.error("Failed to add image " + image.getName(), e);
            return null;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_IMAGE};
    }
}
