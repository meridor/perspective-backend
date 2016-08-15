package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.meridor.perspective.sql.impl.parser.DataSourceUtils.checkLeftDataSource;
import static org.meridor.perspective.sql.impl.parser.DataSourceUtils.checkRightDataSource;

@Component
public class TableScanStrategy extends ScanStrategy {
    
    @Autowired
    private DataFetcher dataFetcher;
    
    @Autowired
    private TablesAware tablesAware;
    
    @Override
    public DataContainer process(DataSource dataSource, Map<String, String> tableAliases) {
        return fetchData(dataSource, tableAliases);
    }

    private DataContainer fetchData(DataSource dataSource, Map<String, String> tableAliases) {
        checkLeftDataSource(dataSource, false);
        String tableAlias = dataSource.getTableAlias().get();
        String tableName = tableAliases.get(tableAlias);
        DataContainer leftData = dataFetcher.fetch(tableName, tableAlias, tablesAware.getColumns(tableName));
        if (dataSource.getRightDataSource().isPresent()) {
            DataSource rightDataSource = dataSource.getRightDataSource().get();
            checkRightDataSource(rightDataSource, false);
            DataContainer rightData = fetchData(rightDataSource, tableAliases);
            return join(leftData, rightDataSource, rightData);
        }
        return leftData;
    }

}
