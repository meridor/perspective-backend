package org.meridor.perspective.sql.impl.table;

import java.util.List;
import java.util.Optional;

public interface TablesAware {

    List<Column> getColumns(TableName tableName);
    
    Optional<Column> getColumn(TableName tableName, String columnName);
    
}
