package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.fetcher.LastModificationAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
class MockFetcher extends BaseFetcher {
    
    private List<Set<String>> fetches = new ArrayList<>();
    
    @Override
    public void fetch(Cloud cloud) {
        fetches.add(Collections.emptySet());
    }

    @Override
    public void fetch(Cloud cloud, Set<String> ids) {
        fetches.add(ids);
    }

    @Override
    protected int getFullSyncDelay() {
        return 1000;
    }

    @Override
    protected LastModificationAware getLastModificationAware() {
        return new MockLastModificationAware();
    }

    public List<Set<String>> getFetches() {
        return fetches;
    }
}
