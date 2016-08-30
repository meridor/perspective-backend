package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.fetcher.Fetcher;
import org.meridor.perspective.worker.fetcher.LastModificationAware;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.function.Consumer;

@Component
public abstract class BaseFetcher<T> implements Fetcher {

    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;
    
    @PostConstruct
    public void init() {
        int fullSyncDelay = getFullSyncDelay();
        scheduleMomentsAgoSync(fullSyncDelay);
        scheduleSomeTimeAgoSync(fullSyncDelay);
        scheduleFullSync(fullSyncDelay);
    }

    private void scheduleMomentsAgoSync(int fullSyncDelay) {
        scheduleIdsFetch(LastModified.MOMENTS_AGO, SchedulerUtils.getMomentsAgoDelay(fullSyncDelay));
    }
    
    private void scheduleSomeTimeAgoSync(int fullSyncDelay) {
        scheduleIdsFetch(LastModified.SOME_TIME_AGO, SchedulerUtils.getSomeTimeAgoDelay(fullSyncDelay));
    }
    
    private void scheduleIdsFetch(LastModified lastModified, int syncDelay) {
        scheduler.scheduleAtFixedRate(
                () -> cloudConfigurationProvider.getClouds().forEach(cloud -> {
                    String cloudId = cloud.getId();
                    Set<String> idsToFetch = getLastModificationAware().getIds(cloudId, lastModified);
                    if (!idsToFetch.isEmpty()) {
                        fetch(cloud, idsToFetch);
                    }
                }),
                syncDelay
        );
    }
    
    private void scheduleFullSync(int fullSyncDelay) {
        scheduler.scheduleAtFixedRate(
                () -> forEachCloud(this::fetch), 
                fullSyncDelay
        );
    }
    
    private Runnable forEachCloud(Consumer<Cloud> action) {
        return () -> cloudConfigurationProvider.getClouds().forEach(action);
    }

    protected abstract int getFullSyncDelay();
    
    protected abstract LastModificationAware<T> getLastModificationAware();

}
