package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.table.TableName;

import java.util.List;

public interface Storage {
    
    List<DataRow> fetch(TableName tableName, Object...expressions);
    
}
