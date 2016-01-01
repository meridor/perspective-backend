package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupTask implements Task {

    private final List<Object> expressions = new ArrayList<>();

    @Autowired
    private ExpressionEvaluator expressionEvaluator;

    public void addExpression(Object expression) {
        this.expressions.add(expression);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        List<DataRow> data = previousTaskResult.getData();
        Map<List<Object>, List<DataRow>> initialMap = new HashMap<List<Object>, List<DataRow>>(){
            {
                put(new ArrayList<>(), data);
            }
        };
        List<DataRow> groupedData = groupData(initialMap, expressions)
                .values().stream().map(v -> v.get(0))
                .collect(Collectors.toList());
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setCount(groupedData.size());
        executionResult.setData(groupedData);
        return executionResult;
    }
    
    private Map<List<Object>, List<DataRow>> groupData(Map<List<Object>, List<DataRow>> previousData, List<Object> remainingExpressions) {
        if (remainingExpressions.isEmpty()) {
            return previousData;
        }
        Object currentExpression = remainingExpressions.remove(0);
        //Map key is a list of grouping expressions, i.e. each value contains only data rows grouped by these expressions
        Map<List<Object>, List<DataRow>> newData = new HashMap<>();
        previousData.keySet().forEach(k -> {
            List<DataRow> currentKeyData = previousData.get(k);
            Map<Object, List<DataRow>> groupedData = currentKeyData.stream()
                    .collect(Collectors.groupingBy( //Here we group by current expression
                            dr -> expressionEvaluator.evaluate(currentExpression, dr)
                    ));
            groupedData.keySet().forEach(gk -> {
                List<Object> newKey = new ArrayList<Object>(){
                    {
                        addAll(k);
                        add(gk);
                    }
                };
                newData.put(newKey, groupedData.get(gk));
            });
        });
        return groupData(newData, remainingExpressions);
    }
    
}
