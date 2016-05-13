package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.impl.table.Column;

import java.util.List;
import java.util.Set;

public interface TableFetcher {

    List<List<Object>> fetch(Set<Column> columns);

    String getTableName();

}
