package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ExpressionEvaluator;
import org.meridor.perspective.sql.impl.expression.OrderDirection;
import org.meridor.perspective.sql.impl.expression.OrderExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
        Optional<Comparator<DataRow>> comparatorCandidate = createComparator(Optional.empty(), expressions);
        if (comparatorCandidate.isPresent()) {
            return new ExecutionResult(){
                {
                    setCount(previousTaskResult.getCount());
                    DataContainer newData = new DataContainer(
                            previousTaskResult.getData(),
                            rows -> rows.stream()
                                    .sorted(comparatorCandidate.get())
                                    .collect(Collectors.toList()) 
                    );
                    setData(newData);
                }
            };
        }
        return previousTaskResult;
    }
    
    private Optional<Comparator<DataRow>> createComparator(Optional<Comparator<DataRow>> comparator, List<OrderExpression> remainingExpressions) {
        if (remainingExpressions.isEmpty()) {
            return comparator;
        }
        OrderExpression currentExpression = remainingExpressions.remove(0);
        Comparator<DataRow> nextComparator = (!comparator.isPresent()) ?
                getComparator(currentExpression) :
                comparator.get().thenComparing(getComparator(currentExpression));
        return createComparator(Optional.of(nextComparator), remainingExpressions);
    }
    
    private Comparator<DataRow> getComparator(OrderExpression orderExpression) {
        Comparator<DataRow> comparator = Comparator.comparing(dr -> expressionEvaluator.evaluate(orderExpression.getExpression(), dr));
        return orderExpression.getOrderDirection() == OrderDirection.ASC ?
                comparator :
                comparator.reversed();
    }

}
