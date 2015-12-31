package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;

import java.util.List;
import java.util.Optional;

public interface FunctionsAware {
    
    Optional<Function<?>> getFunction(String name);
    
}
