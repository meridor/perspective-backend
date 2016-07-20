package org.meridor.perspective.sql.impl.index.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IndexSignature implements Serializable {
    
    private final Map<String, Set<String>> desiredColumns;

    public IndexSignature(String tableName, Set<String> columnNames) {
        this(new HashMap<String, Set<String>>(){
            {
                put(tableName, new LinkedHashSet<>(columnNames));
            }
        });
    }
    
    public IndexSignature(Map<String, Set<String>> desiredColumns) {
        this.desiredColumns = desiredColumns;
    }

    public Map<String, Set<String>> getDesiredColumns() {
        return new HashMap<>(desiredColumns);
    }
    
    public String getValue() {
        return desiredColumns.keySet().stream()
                .map(tn -> 
                        tn + desiredColumns.get(tn).stream()
                                .sorted()
                                .collect(Collectors.joining())
                )
                .collect(Collectors.joining());
    }
    
    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IndexSignature && obj.hashCode() == hashCode();
    }

    @Override
    public String toString() {
        return String.format("IndexSignature{%s}", desiredColumns);
    }
}
