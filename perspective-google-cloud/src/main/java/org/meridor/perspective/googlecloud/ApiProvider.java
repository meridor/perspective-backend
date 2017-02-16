package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.Region;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.RegionsAware;

public interface ApiProvider extends RegionsAware<Region, Api> {

    Api getApi(Cloud cloud);

    @Override
    default String getRegionName(Region region) {
        return region.getRegionId().getRegion();
    }
}
