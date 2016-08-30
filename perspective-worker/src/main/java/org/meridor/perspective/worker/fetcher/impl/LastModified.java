package org.meridor.perspective.worker.fetcher.impl;

public enum LastModified {
    
    MOMENTS_AGO,
    SOME_TIME_AGO,
    LONG_AGO;
    
    public String getKey(String cloudId) {
        return String.format("%s_%s", cloudId, name());
    } 
    
}
