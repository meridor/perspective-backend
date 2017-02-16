package org.meridor.perspective.googlecloud;

import org.meridor.perspective.beans.Image;
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
        return apiProvider.getApi(cloud);
    }

}
