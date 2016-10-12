package org.meridor.perspective.digitalocean;

import org.meridor.perspective.config.Cloud;

import java.util.function.BiConsumer;

public interface ApiProvider {

    Api getApi(Cloud cloud);

    void forEachRegion(Cloud cloud, BiConsumer<String, Api> action) throws Exception;
}
