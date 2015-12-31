package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ExpressionEvaluator;
import org.meridor.perspective.sql.impl.expression.OrderDirection;
import org.meridor.perspective.sql.impl.expression.OrderExpression;
import org.meridor.perspective.sql.impl.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderTask implements Task {
    
    private final List<OrderExpression> expressions = new ArrayList<>();
    
    @Autowired
    private ExpressionEvaluator expressionEvaluator;

    public void addExpression(OrderExpression orderExpression) {
        this.expressions.add(orderExpression);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        Optional<Comparator<DataRow>> comparatorCandidate = createComparator(Optional.empty(), true, expressions);
        if (comparatorCandidate.isPresent()) {
            ExecutionResult executionResult = new ExecutionResult();
            executionResult.setCount(previousTaskResult.getCount());
            List<DataRow> sortedList = previousTaskResult.getData().stream()
                    .sorted(comparatorCandidate.get())
                    .collect(Collectors.toList());
            executionResult.setData(sortedList);
            return executionResult;
        }
        return previousTaskResult;
    }
    
    private Optional<Comparator<DataRow>> createComparator(Optional<Comparator<DataRow>> comparator, boolean isFirstExpression, List<OrderExpression> remainingExpressions) {
        if (remainingExpressions.isEmpty()) {
            return comparator;
        }
        OrderExpression currentExpression = remainingExpressions.remove(0);
        Comparator<DataRow> nextComparator = (isFirstExpression && !comparator.isPresent()) ?
                getComparator(currentExpression) :
                comparator.get().thenComparing(getComparator(currentExpression));
        return createComparator(Optional.of(nextComparator), false, remainingExpressions);
    }
    
    private Comparator<DataRow> getComparator(OrderExpression orderExpression) {
         Comparator<DataRow> comparator = Comparator.comparing(dr -> expressionEvaluator.evaluate(orderExpression.getExpression(), dr));
        return orderExpression.getOrderDirection() == OrderDirection.ASC ?
                comparator :
                comparator.reversed();
    }

}
