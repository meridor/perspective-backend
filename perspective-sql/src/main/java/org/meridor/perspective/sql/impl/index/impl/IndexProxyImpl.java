package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.index.IndexProxy;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class IndexProxyImpl implements IndexProxy {
    
    @Autowired
    private TablesAware tablesAware;
    
    @Autowired
    private DataFetcher dataFetcher;
    
    @Override
    public DataContainer join(DataContainer left, DataContainer right, Object joinCondition) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public DataContainer fetch(String tableName, String tableAlias, List<Column> columns, Object expression) {
        Map<String, Set<String>> desiredColumns = new HashMap<>();
        throw new UnsupportedOperationException("Not implemented!");
    }
    
}
