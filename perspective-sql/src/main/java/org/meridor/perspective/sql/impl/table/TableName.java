package org.meridor.perspective.sql.impl.table;

import java.util.Arrays;
import java.util.Optional;

public enum TableName {
    MOCK,
    INSTANCES;

    public String getTableName() {
        return name().toLowerCase();
    }
    
    public static TableName fromString(String name) {
        Optional<TableName> tableNameCandidate = Arrays.asList(TableName.values()).stream()
                .filter(v -> v.name().equalsIgnoreCase(name)).findFirst();
        if (!tableNameCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format("Table \"%s\" does not exist", name));
        }
        return tableNameCandidate.get();
    }
}
