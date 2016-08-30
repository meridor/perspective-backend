package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.framework.storage.EntityListener;
import org.meridor.perspective.framework.storage.StorageEvent;
import org.meridor.perspective.worker.fetcher.LastModificationAware;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LastModificationListener<T> implements EntityListener<T>, LastModificationAware<T> {
    
    private final Map<String, Set<String>> lastModifiedData = new ConcurrentHashMap<>();
    
    protected abstract int getLongTimeAgoLimit();
    
    protected abstract String getId(T entity);
    
    protected abstract String getCloudId(T entity);
    
    protected abstract Instant getLastModifiedInstant(T entity);

    @Override
    public void onEvent(T entity, T previousEntity, StorageEvent event) {
        LastModified lastModified = getLastModified(entity);
        String id = getId(entity);
        String cloudId = getCloudId(entity);
        String key = lastModified.getKey(cloudId);
        switch (event) {
            case ADDED:
            case MODIFIED: {
                lastModifiedData.compute(key, (k, oldIds) -> new HashSet<String>(){
                    {
                        if (oldIds != null) {
                            addAll(oldIds);
                        }
                        add(id);
                    }
                });
            }
            case DELETED:
            case EVICTED: {
                lastModifiedData.compute(key, (k, oldIds) -> new HashSet<String>(){
                    {
                        if (oldIds != null) {
                            oldIds.remove(id);
                            addAll(oldIds);
                        }
                    }
                });
            }
        }
    }
    
    private LastModified getLastModified(T entity) {
        int longTimeAgoLimit = getLongTimeAgoLimit();
        int momentsAgoLimit = SchedulerUtils.getMomentsAgoLimit(longTimeAgoLimit);
        int someTimeAgoLimit = SchedulerUtils.getSomeTimeAgoLimit(longTimeAgoLimit);
        Instant currentTimestamp = Instant.now();
        Duration difference = Duration.between(currentTimestamp, getLastModifiedInstant(entity));
        long differenceMilliseconds = difference.toMillis();
        if (differenceMilliseconds <= momentsAgoLimit) {
            return LastModified.MOMENTS_AGO;
        } else if (differenceMilliseconds <= someTimeAgoLimit) {
            return LastModified.SOME_TIME_AGO;
        } else {
            return LastModified.LONG_AGO;
        }
    }

    @Override
    public Set<String> getIds(String cloudId, LastModified lastModified) {
        return lastModifiedData.getOrDefault(lastModified.getKey(cloudId), Collections.emptySet());
    }
}
