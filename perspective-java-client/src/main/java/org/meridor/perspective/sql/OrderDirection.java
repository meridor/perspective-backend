package org.meridor.perspective.sql;

public enum OrderDirection {
    ASC("asc"),
    DESC("desc");
    
    private final String value;

    OrderDirection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
