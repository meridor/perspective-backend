package org.meridor.perspective.sql.impl.function;

import java.util.Optional;

public interface FunctionsAware {
    
    Optional<Function<?>> getFunction(String name);
    
}
