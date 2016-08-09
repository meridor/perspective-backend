package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.openstack4j.api.OSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Component
public abstract class BaseInstanceOperation implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseInstanceOperation.class);

    @Autowired
    private OpenstackApiProvider apiProvider;

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        try {
            Instance instance = supplier.get();
            String region = instance.getMetadata().get(MetadataKey.REGION);
            OSClient api = apiProvider.getApi(cloud, region);
            if (region == null) {
                Project project = projectsAware.getProject(instance.getProjectId()).get();
                region = project.getMetadata().get(MetadataKey.REGION);
            }
            api.useRegion(region);
            getAction().accept(api, instance);
            LOG.debug(getSuccessMessage(instance));
            return true;
        } catch (Exception e) {
            LOG.error(getErrorMessage(), e);
            return false;
        }
    }
    
    protected abstract BiConsumer<OSClient, Instance> getAction();

    protected abstract String getSuccessMessage(Instance instance);

    protected abstract String getErrorMessage();
    
}
