package org.meridor.perspective.sql.impl.table;

public class Column {

    public static final String ANY = "*";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        return name.equals(column.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
