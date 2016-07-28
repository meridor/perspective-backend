package org.meridor.perspective.sql.impl.table;

import java.util.Optional;
import java.util.Set;

public interface TablesAware {

    Set<String> getTables();

    Set<Column> getColumns(String tableName);
    
    Optional<Column> getColumn(String tableName, String columnName);
    
}
