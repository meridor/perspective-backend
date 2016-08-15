package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.impl.table.Column;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TableFetcher {

    Map<String, List<Object>> fetch(Set<String> ids, Collection<Column> columns);

    String getTableName();

}
