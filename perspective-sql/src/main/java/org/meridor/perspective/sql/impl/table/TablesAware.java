package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.Index;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TablesAware {

    Optional<Index> getIndex(Map<String, Set<Column>> desiredColumns);
    
    Set<String> getTables();
    
    List<Column> getColumns(String tableName);
    
    Optional<Column> getColumn(String tableName, String columnName);
    
}
