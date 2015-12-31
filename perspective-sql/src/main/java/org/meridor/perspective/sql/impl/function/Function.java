package org.meridor.perspective.sql.impl.function;

import java.util.List;
import java.util.Set;

public interface Function<O> extends java.util.function.Function<List<Object>, O> {
    
    Set<String> validateInput(List<Object> args);
    
    Class<O> getReturnType();
    
    FunctionName getName();
    
}
