package org.meridor.perspective.rest;

import java.io.Serializable;

public class Event implements Serializable {
    
    private String key;
    
    public Event(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
}