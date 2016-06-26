package org.meridor.perspective.sql.impl.task;


import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.task.strategy.DataSourceStrategy;
import org.meridor.perspective.sql.impl.task.strategy.ParentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.meridor.perspective.sql.impl.parser.DataSource.DataSourceType.PARENT;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataSourceTask implements Task {
    
    @Autowired
    private ApplicationContext applicationContext;

    private final DataSource dataSource;
    private final Map<String, String> tableAliases = new HashMap<>();

    public DataSourceTask(DataSource dataSource, Map<String, String> tableAliases) {
        this.dataSource = dataSource;
        this.tableAliases.putAll(tableAliases);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        try {
            DataContainer result = processDataSource(dataSource, tableAliases);
            ExecutionResult executionResult = new ExecutionResult() {
                {
                    setData(result);
                    setCount(result.getRows().size());
                }
            };
            return executionResult;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private DataContainer processDataSource(DataSource dataSource, Map<String, String> tableAliases) {
        if (dataSource.getType() != PARENT) {
            throw new IllegalArgumentException("Data source task accepts only parent data sources");
        }
        DataSourceStrategy dataSourceStrategy = applicationContext.getBean(ParentStrategy.class);
        return dataSourceStrategy.process(dataSource, tableAliases);
    }

}
