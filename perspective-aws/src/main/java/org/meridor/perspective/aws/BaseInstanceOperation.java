package org.meridor.perspective.aws;

import com.amazonaws.regions.Regions;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.AbstractInstanceOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseInstanceOperation extends AbstractInstanceOperation<Api> {

    @Autowired
    private ApiProvider apiProvider;

    @Override
    protected Api getApi(Cloud cloud, Instance instance) {
        Regions region = Regions.fromName(instance.getMetadata().get(MetadataKey.REGION));
        return apiProvider.getApi(cloud, region);
    }

}
