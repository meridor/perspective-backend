package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FilterTask implements Task {

    private Predicate<DataRow> condition = r -> true;
    
    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        List<DataRow> newData = previousTaskResult.getData().stream()
                .filter(condition)
                .collect(Collectors.toList());
        return new ExecutionResult(){
            {
                setCount(newData.size());
                setData(newData);
            }
        };
    }

    public void setCondition(Predicate<DataRow> condition) {
        this.condition = condition;
    }
}
