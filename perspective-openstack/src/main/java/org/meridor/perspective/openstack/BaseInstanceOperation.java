package org.meridor.perspective.openstack;

import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.AbstractInstanceOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseInstanceOperation extends AbstractInstanceOperation<Api> {

    @Autowired
    private ApiProvider apiProvider;

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Api getApi(Cloud cloud, Instance instance) {
        String region = instance.getMetadata().get(MetadataKey.REGION);
        if (region == null) {
            Project project = projectsAware.getProject(instance.getProjectId()).get();
            region = project.getMetadata().get(MetadataKey.REGION);
        }
        return apiProvider.getApi(cloud, region);
    }

}
