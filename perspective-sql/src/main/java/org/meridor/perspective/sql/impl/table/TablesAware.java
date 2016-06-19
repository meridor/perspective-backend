package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TablesAware {

    Set<IndexSignature> getIndexSignatures();
    
    Optional<Index> getIndex(IndexSignature indexSignature);
    
    Set<String> getTables();

    List<Column> getColumns(String tableName);
    
    Optional<Column> getColumn(String tableName, String columnName);
    
}
