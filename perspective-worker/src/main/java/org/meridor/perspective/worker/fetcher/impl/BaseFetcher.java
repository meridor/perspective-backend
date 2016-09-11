package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.fetcher.Fetcher;
import org.meridor.perspective.worker.fetcher.LastModificationAware;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.worker.fetcher.impl.LastModified.*;
import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.*;

@Component
public abstract class BaseFetcher implements Fetcher {

    private static final Logger LOG = LoggerFactory.getLogger(BaseFetcher.class);
    
    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;
    
    @PostConstruct
    public void init() {
        int fullSyncDelay = getFullSyncDelay();
        scheduleNowSync(fullSyncDelay);
        scheduleMomentsAgoSync(fullSyncDelay);
        scheduleSomeTimeAgoSync(fullSyncDelay);
        scheduleFullSync(fullSyncDelay);
    }

    private void scheduleNowSync(int fullSyncDelay) {
        int nowDelay = getNowDelay(fullSyncDelay);
        LOG.debug(
                "{} will fetch entities modified NOW every {} milliseconds",
                getClass().getSimpleName(),
                nowDelay
        );
        scheduleIdsFetch(NOW, nowDelay);
    }
    
    private void scheduleMomentsAgoSync(int fullSyncDelay) {
        int momentsAgoDelay = getMomentsAgoDelay(fullSyncDelay);
        scheduleIdsFetch(MOMENTS_AGO, momentsAgoDelay);
        LOG.debug(
                "{} will fetch entities modified MOMENTS AGO every {} milliseconds",
                getClass().getSimpleName(),
                momentsAgoDelay
        );
    }
    
    private void scheduleSomeTimeAgoSync(int fullSyncDelay) {
        int someTimeAgoDelay = getSomeTimeAgoDelay(fullSyncDelay);
        scheduleIdsFetch(SOME_TIME_AGO, someTimeAgoDelay);
        LOG.debug(
                "{} will fetch entities modified SOME TIME AGO every {} milliseconds",
                getClass().getSimpleName(),
                someTimeAgoDelay
        );
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
                forEachCloud(this::fetch), 
                fullSyncDelay
        );
        LOG.debug(
                "{} will fetch all entities every {} milliseconds",
                getClass().getSimpleName(),
                fullSyncDelay
        );
    }
    
    private Runnable forEachCloud(Consumer<Cloud> action) {
        return () -> cloudConfigurationProvider.getClouds().forEach(action);
    }

    protected abstract int getFullSyncDelay();
    
    protected abstract LastModificationAware getLastModificationAware();

}
