package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;

import java.util.List;

public interface Storage {
    
    DataContainer fetch(TableName tableName, String tableAlias, List<Column> columns);
    
}
