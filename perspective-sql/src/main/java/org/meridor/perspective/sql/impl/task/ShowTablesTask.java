package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShowTablesTask implements Task {
    
    private static final String TABLE_NAME = "table_name";
    
    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        List<DataRow> data = Arrays.stream(TableName.values())
                .map(tn -> new DataRow(){
                    {
                        put(TABLE_NAME, tn.getTableName());
                    }
                })
                .collect(Collectors.toList());
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setCount(data.size());
        executionResult.setData(data);
        return executionResult;
    }
    
}
