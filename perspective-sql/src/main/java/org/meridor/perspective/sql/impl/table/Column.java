package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.impl.IndexSignature;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Column {

    public static final String ANY_COLUMN = "*";
    public static final String ANY_TABLE = ANY_COLUMN;
    private final String name;
    private final Class<?> type;
    private final Object defaultValue;
    private final Table table;

    public Column(String name, Class<?> type, Object defaultValue, Table table) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.table = table;
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

    public Table getTable() {
        return table;
    }

    public Set<IndexSignature> getIndexes(Set<IndexSignature> allSignatures) {
        return allSignatures.stream()
                .filter(s -> s.getDesiredColumns()
                        .getOrDefault(getTable().getName(), Collections.emptySet())
                        .contains(getName())
                )
                .collect(Collectors.toSet());
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
