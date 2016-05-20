package org.meridor.perspective.sql.impl.index;

public interface Indexer {
    
    void add(String tableName, Object bean);
    
    void delete(String tableName, Object bean);
    
}
