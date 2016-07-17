package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.table.Column;

import java.util.Collection;
import java.util.Set;

public interface DataFetcher {

    DataContainer fetch(String tableName, String tableAlias, Set<String> ids, Collection<Column> columns);
    
    DataContainer fetch(String tableName, String tableAlias, Collection<Column> columns);

}
