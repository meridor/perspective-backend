package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.openstack4j.api.OSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class DeleteInstanceOperation implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteInstanceOperation.class);

    @Autowired
    private OpenstackApiProvider apiProvider;

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        try {
            Instance instance = supplier.get();
            String region = instance.getMetadata().get(MetadataKey.REGION);
            OSClient.OSClientV3 api = apiProvider.getApi(cloud, region);
            if (region == null) {
                Project project = projectsAware.getProject(instance.getProjectId()).get();
                region = project.getMetadata().get(MetadataKey.REGION);
            }
            api.useRegion(region);
            String instanceId = instance.getRealId();
            api.compute().servers().delete(instanceId);
            LOG.debug("Deleted instance {} ({})", instance.getName(), instance.getId());
            return true;
        } catch (Exception e) {
            LOG.error("Failed to delete instance", e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{OperationType.DELETE_INSTANCE};
    }
}
