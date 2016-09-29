package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.fetcher.LastModificationAware;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
class MockFetcher extends BaseFetcher {

    private final Map<String, Integer> fetches = new ConcurrentHashMap<>();
    
    @Override
    public void fetch(Cloud cloud) {
        increase("all");
    }

    @Override
    public void fetch(Cloud cloud, Set<String> ids) {
        ids.forEach(this::increase);
    }

    private void increase(String key) {
        fetches.putIfAbsent(key, 0);
        fetches.put(key, fetches.get(key) + 1);
    }
    
    @Override
    protected int getFullSyncDelay() {
        return 600; //Can be divided by all SchedulerUtils constants so all limits are integers
    }

    @Override
    protected LastModificationAware getLastModificationAware() {
        return (cloudId, lastModified) -> {
            switch (lastModified) {
                case NOW: return Collections.singleton("now");
                case MOMENTS_AGO: return Collections.singleton("moments");
                case SOME_TIME_AGO: return Collections.singleton("some_time");
                default:
                case LONG_AGO: return Collections.singleton("long");
            }
        };
    }

    int getFetches(String key) {
        return fetches.getOrDefault(key, 0);
    }

}
