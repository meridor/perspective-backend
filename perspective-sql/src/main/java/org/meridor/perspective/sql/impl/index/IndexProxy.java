package org.meridor.perspective.sql.impl.index;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.table.Column;

import java.util.List;

public interface IndexProxy {
    
    //Using indexes in joins
    DataContainer join(DataContainer left, DataContainer right, Object joinCondition);
    
    //Using indexes in simple data fetching
    DataContainer fetch(String tableName, String tableAlias, List<Column> columns, Object expression);
    
}
