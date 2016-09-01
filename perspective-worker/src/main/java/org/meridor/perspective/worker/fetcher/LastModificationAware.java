package org.meridor.perspective.worker.fetcher;

import org.meridor.perspective.worker.fetcher.impl.LastModified;

import java.util.Set;

public interface LastModificationAware {
    
    Set<String> getIds(String cloudId, LastModified lastModified);

}
