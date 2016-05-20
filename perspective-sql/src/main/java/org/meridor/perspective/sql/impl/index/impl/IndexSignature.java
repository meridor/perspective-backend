package org.meridor.perspective.sql.impl.index.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IndexSignature {
    
    private final Map<String, Set<String>> desiredColumns;

    public IndexSignature(Map<String, Set<String>> desiredColumns) {
        this.desiredColumns = desiredColumns;
    }

    public Map<String, Set<String>> getDesiredColumns() {
        return desiredColumns;
    }
    
    public String getValue() {
        return desiredColumns.keySet().stream()
                .map(tn -> tn + desiredColumns.get(tn).stream().collect(Collectors.joining()))
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
        return "IndexSignature{" + getValue() + "}";
    }
}
