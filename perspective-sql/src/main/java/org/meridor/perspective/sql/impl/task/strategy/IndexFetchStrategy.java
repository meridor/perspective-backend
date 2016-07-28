package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IndexFetchStrategy implements DataSourceStrategy {
    
    @Autowired
    private TablesAware tablesAware;
    
    @Autowired
    private IndexStorage indexStorage;
    
    @Override
    public DataContainer process(DataSource dataSource, Map<String, String> tableAliases) {
        if (dataSource.getRightDataSource().isPresent()) {
            throw new IllegalArgumentException("Index fetch strategy can not process joins");
        }
        if (!dataSource.getTableAlias().isPresent()) {
            throw new IllegalArgumentException("Index fetch strategy datasource should contain table alias");
        }
        List<String> columnNames = dataSource.getColumns();
        if (columnNames.isEmpty()) {
            throw new IllegalArgumentException("Column names are required for index fetch strategy");
        }
        String tableAlias = dataSource.getTableAlias().get();
        String tableName = tableAliases.get(tableAlias);
        IndexSignature indexSignature = new IndexSignature(tableName, new LinkedHashSet<>(columnNames));
        Optional<Index> indexCandidate = indexStorage.get(indexSignature);
        if (!indexCandidate.isPresent()) {
            throw new IllegalStateException(String.format("Index for table \"%s\" and column names \"%s\" was not found", tableName, columnNames));
        }
        Index index = indexCandidate.get();
        DataContainer result = new DataContainer(Collections.singletonMap(tableAlias, columnNames));
        index.getKeys().forEach(k -> result.addRow(k.getValues()));
        return result;
    }
    
}
