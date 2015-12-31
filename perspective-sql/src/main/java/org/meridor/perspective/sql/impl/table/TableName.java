package org.meridor.perspective.sql.impl.table;

import java.util.Arrays;
import java.util.Optional;

public enum TableName {
    MOCK,
    INSTANCES;

    public String getTableName() {
        return name().toLowerCase();
    }
    
    public static Optional<TableName> fromString(String name) {
        return Arrays.asList(TableName.values()).stream()
                .filter(v -> v.name().equalsIgnoreCase(name)).findFirst();
    }
}
