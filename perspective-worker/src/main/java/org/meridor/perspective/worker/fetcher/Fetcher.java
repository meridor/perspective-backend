package org.meridor.perspective.worker.fetcher;

import org.meridor.perspective.config.Cloud;

import java.util.Set;

public interface Fetcher {
    
    void fetch(Cloud cloud);
    
    void fetch(Cloud cloud, Set<String> ids);
    
}
