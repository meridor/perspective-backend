package org.meridor.perspective.rest;

import java.io.Serializable;

public class CountEvent implements Serializable {
    
    private long timestamp;
    
    public CountEvent(long timestamp) {
        this.timestamp = timestamp;
    }
    public long getTimestamp() {
        return timestamp;
    }
}