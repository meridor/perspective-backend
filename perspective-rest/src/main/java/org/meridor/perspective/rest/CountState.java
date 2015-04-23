package org.meridor.perspective.rest;

import java.io.Serializable;

public class CountState implements Serializable {
    
    private Integer count = 0;

    public Integer getCount() {
        return count;
    }
    
    public void increment() {
        count++;
    }
}
