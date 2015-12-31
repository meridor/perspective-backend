package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LimitTask implements Task {
    
    private final int offset;
    
    private final int count;

    public LimitTask(int offset, int count) {
        this.offset = offset;
        this.count = count;
    }
    
    public LimitTask(int count) {
        this(0, count);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        ExecutionResult executionResult = new ExecutionResult();
        List<DataRow> newData = previousTaskResult.getData().stream()
                .skip(offset)
                .limit(count)
                .collect(Collectors.toList());
        executionResult.setData(newData);
        executionResult.setCount(newData.size());
        return executionResult;
    }
    
}
