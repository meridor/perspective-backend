package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.storage.Storage;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectTask implements Task {
    
    @Autowired
    private Storage storage;
    
    private final String tableName;
    
    private final List<String> columnNames = new ArrayList<>();
    
    private Predicate<DataRow> condition = r -> true;

    @Autowired
    public SelectTask(String tableName, List<String> columnNames) {
        this.tableName = tableName;
        this.columnNames.addAll(columnNames);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        Optional<TableName> tableName = TableName.fromString(this.tableName);
        return (tableName.isPresent()) ?
                new ExecutionResult(){
                    {
                        List<DataRow> data = storage
                                .fetch(tableName.get(), columnNames).stream()
                                .filter(condition)
                                .collect(Collectors.toList());
                        setCount(data.size());
                        setData(data);
                    }
                } :
                new ExecutionResult();
    }

    public void setCondition(Predicate<DataRow> condition) {
        this.condition = condition;
    }
}
