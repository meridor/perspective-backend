package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.BooleanExpression;
import org.meridor.perspective.sql.impl.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FilterTask implements Task {

    private BooleanExpression condition;
    
    @Autowired
    private ExpressionEvaluator expressionEvaluator;
    
    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        try {
            Predicate<DataRow> predicate = condition != null ?
                    dr -> expressionEvaluator.evaluateAs(condition, dr, Boolean.class) :
                    dr -> true;
            DataContainer newData = new DataContainer(
                    previousTaskResult.getData(),
                    rows -> rows.stream().filter(predicate).collect(Collectors.toList())
            );
            return new ExecutionResult(){
                {
                    setCount(newData.getRows().size());
                    setData(newData);
                }
            };
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public void setCondition(BooleanExpression condition) {
        this.condition = condition;
    }

    public Optional<BooleanExpression> getCondition() {
        return Optional.ofNullable(condition);
    }

    @Override
    public String toString() {
        return "FilterTask{" +
                "condition=" + condition +
                '}';
    }
}
