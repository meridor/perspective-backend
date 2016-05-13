package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.Index;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TablesAware {

    Optional<Index> getIndex(Map<String, Set<String>> desiredColumns);
    
    Set<String> getTables();

    Set<Column> getColumns(String tableName);
    
    Optional<Column> getColumn(String tableName, String columnName);
    
}
