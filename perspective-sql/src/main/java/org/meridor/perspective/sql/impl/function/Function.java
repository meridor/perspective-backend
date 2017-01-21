package org.meridor.perspective.sql.impl.function;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface Function<O> extends java.util.function.Function<List<Object>, O> {
    
    default Set<String> validateInput(List<Object> args) {
        if (!args.isEmpty()) {
            return Collections.singleton("Function should be called without arguments");
        }
        return Collections.emptySet();
    }
    
    Class<O> getReturnType();
    
    FunctionName getName();

    default String getSignature() {
        return String.format("%s()", getName().name());
    }

    default String getDescription() {
        return "";
    }
    
}
