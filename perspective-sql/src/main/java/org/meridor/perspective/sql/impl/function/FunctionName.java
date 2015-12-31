package org.meridor.perspective.sql.impl.function;

import java.util.Arrays;
import java.util.Optional;

public enum FunctionName {
    
    ABS;

    public static Optional<FunctionName> fromString(String name) {
        return Arrays.asList(values()).stream()
                .filter(v -> v.name().equalsIgnoreCase(name)).findFirst();
    }
}
