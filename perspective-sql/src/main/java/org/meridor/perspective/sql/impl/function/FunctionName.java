package org.meridor.perspective.sql.impl.function;

import java.util.Arrays;
import java.util.Optional;

public enum FunctionName {
    
    ABS,
    COLUMNS,
    TABLES,
    TYPEOF,
    VERSION;

    public static Optional<FunctionName> fromString(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(name)).findFirst();
    }
}
