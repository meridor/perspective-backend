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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderTask implements Task {
    
    private final List<OrderExpression> expressions = new ArrayList<>();

    private final ExpressionEvaluator expressionEvaluator;

    @Autowired
    public OrderTask(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    public void addExpression(OrderExpression orderExpression) {
        this.expressions.add(orderExpression);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        try {
            Comparator<DataRow> comparator = createComparator(null, expressions);
            if (comparator != null) {
                return new ExecutionResult(){
                    {
                        setCount(previousTaskResult.getCount());
                        DataContainer newData = new DataContainer(
                                previousTaskResult.getData(),
                                rows -> rows.stream()
                                        .sorted(comparator)
                                        .collect(Collectors.toList()) 
                        );
                        setData(newData);
                    }
                };
            }
            return previousTaskResult;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    private Comparator<DataRow> createComparator(Comparator<DataRow> comparator, List<OrderExpression> remainingExpressions) {
        if (remainingExpressions.isEmpty()) {
            return comparator;
        }
        OrderExpression currentExpression = remainingExpressions.remove(0);
        Comparator<DataRow> nextComparator = comparator == null ?
                getComparator(currentExpression) :
                comparator.thenComparing(getComparator(currentExpression));
        return createComparator(nextComparator, remainingExpressions);
    }

    private Comparator<DataRow> getComparator(OrderExpression orderExpression) {
        Comparator<DataRow> comparator = getComparator(
                dr -> expressionEvaluator.evaluate(orderExpression.getExpression(), dr)
        );
        return orderExpression.getOrderDirection() == OrderDirection.ASC ?
                comparator :
                comparator.reversed();
    }

    private <T, U extends Comparable<? super U>> Comparator<T> getComparator(
            Function<? super T, ? extends U> keyExtractor
    ) {
        //Currently we use nullsLast policy. We may want to change this
        //if respective SQL expressions like NULLS FIRST are introduced.
        return (Comparator<T> & Serializable)
                (c1, c2) -> {
                    U left = keyExtractor.apply(c1);
                    U right = keyExtractor.apply(c2);
                    if (left == null) {
                        return (right == null) ? 0 : 1;
                    } else if (right == null) {
                        return -1;
                    }
                    return left.compareTo(right);
                };
    }

    public List<OrderExpression> getExpressions() {
        return new ArrayList<>(expressions);
    }

    @Override
    public String toString() {
        return "OrderTask{" +
                "expressions=" + expressions +
                '}';
    }
}
