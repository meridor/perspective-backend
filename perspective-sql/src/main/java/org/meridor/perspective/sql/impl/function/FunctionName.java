package org.meridor.perspective.sql.impl.function;

import java.util.Arrays;
import java.util.Optional;

public enum FunctionName {
    
    ABS,
    ACOS,
    ASIN,
    ATAN,
    BASE64,
    BIT_LENGTH,
    CEIL,
    CBRT,
    CHAR,
    CHAR_LENGTH,
    CONV,
    CONCAT,
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
    JOIN,
    LN,
    LOG,
    LOG10,
    LOWER,
    MOD,
    PI,
    POWER,
    RADIANS,
    RAND,
    REPLACE,
    REVERSE,
    ROUND,
    SIGN,
    SIN,
    SQRT,
    SUBSTR,
    TABLES,
    TAN,
    TRIM,
    TRUNCATE,
    TYPEOF,
    UPPER,
    VERSION;

    public static Optional<FunctionName> fromString(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(name)).findFirst();
    }
}
