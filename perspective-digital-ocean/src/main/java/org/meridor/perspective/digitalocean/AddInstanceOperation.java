package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Key;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.ProcessingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.ADD_INSTANCE;

@Component
public class AddInstanceOperation implements ProcessingOperation<Instance, Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AddInstanceOperation.class);

    @Autowired
    private ApiProvider apiProvider;

    @Autowired
    private ProjectsAware projectsAware;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    public Instance perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        String projectId = instance.getProjectId();
        Project project = projectsAware.getProject(projectId).get();
        try {
            String region = project.getMetadata().get(MetadataKey.REGION);
            Api api = apiProvider.getApi(cloud);
            String realId = String.valueOf(api.addDroplet(getDroplet(instance)));
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

    private static Droplet getDroplet(Instance instance) {
        Droplet droplet = new Droplet();
        droplet.setName(instance.getName());

        Image image = new Image();
        image.setId(Integer.valueOf(instance.getImage().getRealId()));
        droplet.setImage(image);

        //TODO: specify flavor (i.e. size, kernel and so on...)

        List<Keypair> keypairs = instance.getKeypairs();
        if (!keypairs.isEmpty()) {
            List<Key> keys = keypairs.stream().map(kp -> {
                Key key = new Key();
                key.setName(kp.getName());
                return key;
            }).collect(Collectors.toList());
            droplet.setKeys(keys);
        }

        return droplet;
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{ADD_INSTANCE};
    }
}
