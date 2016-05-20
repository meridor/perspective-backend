package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.impl.table.Column;

import java.util.List;

public interface TableFetcher {

    List<List<Object>> fetch(List<Column> columns);
    
    List<List<Object>> fetch(List<String> ids, List<Column> columns);

    String getTableName();

}
