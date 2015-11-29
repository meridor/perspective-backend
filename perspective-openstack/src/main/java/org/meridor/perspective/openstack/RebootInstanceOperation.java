package org.meridor.perspective.openstack;

import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.RebootType;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Supplier;

import static org.meridor.perspective.config.OperationType.REBOOT_INSTANCE;

@Component
public class RebootInstanceOperation implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(RebootInstanceOperation.class);
    
    @Autowired
    private OpenstackApiProvider apiProvider;

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud)) {
            Instance instance = supplier.get();
            String region = instance.getMetadata().get(MetadataKey.REGION);
            if (region == null) {
                Project project = projectsAware.getProject(instance.getProjectId()).get();
                region = project.getMetadata().get(MetadataKey.REGION);
            }
            ServerApi serverApi = novaApi.getServerApi(region);
            serverApi.reboot(instance.getRealId(), getRebootType());
            LOG.debug(getSuccessMessage(), instance.getName(), instance.getId());
            return true;
        } catch (IOException e) {
            LOG.error(getErrorMessage(), e);
            return false;
        }
    }
    
    protected RebootType getRebootType() {
        return RebootType.SOFT;
    }

    protected String getSuccessMessage() {
        return "Rebooted instance {} ({})";
    }
    
    protected String getErrorMessage() {
        return "Failed to reboot instance";
    }
    
    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{REBOOT_INSTANCE};
    }
}
