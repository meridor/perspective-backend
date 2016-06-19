package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParentStrategy extends ScanStrategy {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public DataContainer process(DataSource dataSource, Map<String, String> tableAliases) {
        if (!dataSource.getLeftDataSource().isPresent()) {
            throw new IllegalArgumentException("Left data source should be always present in parent data source");
        }
        DataContainer left = fetchData(dataSource.getLeftDataSource().get(), tableAliases);
        if (dataSource.getRightDataSource().isPresent()) {
            DataSource rightDataSource = dataSource.getRightDataSource().get();
            DataContainer right = fetchData(rightDataSource, tableAliases);
            return join(left, rightDataSource, right);
        }
        return left;
    }

    private DataContainer fetchData(DataSource dataSource, Map<String, String> tableAliases) {
        switch (dataSource.getType()) {
            default:
            case PARENT: return process(dataSource, tableAliases);
            case INDEX_FETCH: return fetchDataFromStrategy(IndexFetchStrategy.class, dataSource, tableAliases);
            case INDEX_SCAN: return fetchDataFromStrategy(IndexScanStrategy.class, dataSource, tableAliases);
            case TABLE_SCAN: return fetchDataFromStrategy(TableScanStrategy.class, dataSource, tableAliases);
        }
    }

    private <T extends DataSourceStrategy> DataContainer fetchDataFromStrategy(Class<T> cls, DataSource dataSource, Map<String, String> tableAliases) {
        T dataSourceStrategy = applicationContext.getBean(cls);
        return dataSourceStrategy.process(dataSource, tableAliases);
    }
    
}
