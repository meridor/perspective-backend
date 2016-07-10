package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.impl.IndexSignature;

import java.util.HashSet;
import java.util.Set;

public class Column {

    public static final String ANY = "*";
    private final String name;
    private final Class<?> type;
    private final Object defaultValue;
    private final Set<IndexSignature> indexes = new HashSet<>();

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

    public void addIndex(IndexSignature indexSignature) {
        indexes.add(indexSignature);
    }
    
    public Set<IndexSignature> getIndexes() {
        return new HashSet<>(indexes);
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
