package org.meridor.perspective.docker;

import org.jclouds.docker.DockerApi;
import org.jclouds.docker.domain.Config;
import org.jclouds.docker.domain.Container;
import org.jclouds.docker.features.ContainerApi;
import org.meridor.perspective.beans.Instance;
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
import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;

@Component
public class AddInstanceOperation implements ProcessingOperation<Instance, Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AddInstanceOperation.class);

    @Autowired
    private DockerApiProvider apiProvider;

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        try (DockerApi dockerApi = apiProvider.getApi(cloud)) {
            ContainerApi containerApi = dockerApi.getContainerApi();
            String command = instance.getMetadata().get(MetadataKey.COMMAND);
            String imageId = instance.getImage().getMetadata().get(MetadataKey.ID);
            Config containerConfig = Config.builder()
                    .cmd(new ArrayList<String>() {
                        {
                            add(command);
                        }
                    })
                    .image(imageId)
                    .build();
            Container createdContainer = containerApi.createContainer(instance.getName(), containerConfig);
            
            String instanceId = createdContainer.id();
            instance.getMetadata().put(MetadataKey.ID, instanceId);
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
