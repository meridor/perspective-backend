package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.worker.fetcher.LastModificationAware;

import java.util.Collections;
import java.util.Set;

class MockLastModificationAware implements LastModificationAware {

    @Override
    public Set<String> getIds(String cloudId, LastModified lastModified) {
        switch (lastModified) {
            case NOW: return Collections.singleton("one");
            case MOMENTS_AGO: return Collections.singleton("two");
            case SOME_TIME_AGO: return Collections.singleton("three");
            default:
            case LONG_AGO: return Collections.singleton("four");
        }
    }
}
