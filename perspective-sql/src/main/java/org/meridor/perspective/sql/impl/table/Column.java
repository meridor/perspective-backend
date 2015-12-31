package org.meridor.perspective.sql.impl.table;

public class Column {
    
    private final String name;
    private final Class<?> type;
    private final Object defaultValue;

    public Column(String name, Class<?> type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
