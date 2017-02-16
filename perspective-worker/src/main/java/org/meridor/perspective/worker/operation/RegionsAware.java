package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;

import java.util.function.BiConsumer;

public interface RegionsAware<R, A> {

    void forEachRegion(Cloud cloud, BiConsumer<R, A> action) throws Exception;

    default String getRegionName(R region) {
        return String.valueOf(region);
    }

}
