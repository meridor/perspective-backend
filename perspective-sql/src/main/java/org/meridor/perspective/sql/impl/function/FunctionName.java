package org.meridor.perspective.sql.impl.function;

import java.util.Arrays;
import java.util.Optional;

public enum FunctionName {
    
    ABS,
    ACOS,
    ASIN,
    ATAN,
    CEIL,
    CBRT,
    CONV,
    COS,
    COT,
    CRC32,
    COLUMNS,
    DEGREES,
    E,
    EXP,
    FLOOR,
    FORMAT,
    FUNCTIONS,
    LN,
    LOG,
    LOG10,
    MOD,
    PI,
    POWER,
    RADIANS,
    RAND,
    ROUND,
    TABLES,
    TRUNCATE,
    TYPEOF,
    SIGN,
    SIN,
    SQRT,
    TAN,
    VERSION;

    public static Optional<FunctionName> fromString(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(name)).findFirst();
    }
}
