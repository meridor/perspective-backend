package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.storage.Storage;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectTask implements Task {
    
    @Autowired
    private Storage storage;
    
    @Autowired
    private TablesAware tablesAware;
    
    private final String tableName;
    
    private final List<String> columnNames = new ArrayList<>();
    
    @Autowired
    public SelectTask(String tableName, List<String> columnNames) {
        this.tableName = tableName;
        this.columnNames.addAll(columnNames);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        Optional<TableName> tableNameCandidate = TableName.fromString(this.tableName);
        return (tableNameCandidate.isPresent()) ?
                new ExecutionResult(){
                    {
                        TableName tableName = tableNameCandidate.get();
                        List<String> columnsToSelect = columnNames.isEmpty() ?
                                tablesAware.getColumns(tableName).stream()
                                        .map(Column::getName)
                                        .collect(Collectors.toList()) : 
                                columnNames;
                        List<DataRow> data = storage.fetch(tableName, columnsToSelect);
                        setCount(data.size());
                        setData(data);
                    }
                } :
                new ExecutionResult();
    }

}
