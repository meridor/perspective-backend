package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.AbstractImageOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseImageOperation extends AbstractImageOperation<Api> {

    @Autowired
    private ApiProvider apiProvider;

    @Override
    protected Api getApi(Cloud cloud, Image image) {
        String region = image.getMetadata().get(MetadataKey.REGION);
        return apiProvider.getApi(cloud, region);
    }

}
