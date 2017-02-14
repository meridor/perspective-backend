package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.Region;
import org.meridor.perspective.config.Cloud;

import java.util.function.BiConsumer;

public interface ApiProvider {

    Api getApi(Cloud cloud);

    void forEachRegion(Cloud cloud, BiConsumer<Region, Api> action) throws Exception;

}
