package org.meridor.perspective.aws;

import com.amazonaws.regions.Regions;
import org.meridor.perspective.config.Cloud;

import java.util.function.BiConsumer;

public interface ApiProvider {

    Api getApi(Cloud cloud, Regions region);

    void forEachRegion(Cloud cloud, BiConsumer<String, Api> action) throws Exception;

}
