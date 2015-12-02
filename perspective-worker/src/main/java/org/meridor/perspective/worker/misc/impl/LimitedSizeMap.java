package org.meridor.perspective.worker.misc.impl;

import java.util.LinkedHashMap;
import java.util.Map;

public class LimitedSizeMap<K, V> extends LinkedHashMap<K, V> {

    private final int maxSize;

    public LimitedSizeMap(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
    
}
