package org.meridor.perspective.rest.data.beans;

public class Cloud {
    
    private final String id;
    
    private final String type;

    public Cloud(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
