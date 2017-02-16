package org.meridor.perspective.openstack;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.RegionsAware;

public interface ApiProvider extends RegionsAware<String, Api> {

    Api getApi(Cloud cloud, String region);

}
