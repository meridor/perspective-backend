package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.function.Function;
import org.meridor.perspective.sql.impl.function.FunctionsAware;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExpressionEvaluatorImpl implements ExpressionEvaluator {
    
    @Autowired
    private TablesAware tablesAware;
    
    @Autowired
    private FunctionsAware functionsAware;

    @Override
    public Map<String, List<String>> getColumnNames(Object expression) {
        if (expression instanceof ColumnExpression) {
            ColumnExpression columnExpression = (ColumnExpression) expression;
            String tableName = columnExpression.getTableName();
            String columnName = columnExpression.getColumnName();
            return Collections.singletonMap(tableName, Collections.singletonList(columnName));
        } else if (expression instanceof FunctionExpression) {
            final Map<String, List<String>> ret = new HashMap<>();
            FunctionExpression functionExpression = (FunctionExpression) expression;
            functionExpression.getArgs().stream()
            .map(this::getColumnNames)
            .forEach(m -> m.keySet().forEach(k -> {
                if (ret.containsKey(k)) {
                    ret.get(k).addAll(m.get(k));
                } else {
                    ret.put(k, m.get(k));
                }
            }));
            return ret;
        }
        return Collections.emptyMap();
    }

    @Override
    public <T extends Comparable<? super T>> T evaluate(Object expression, DataRow dataRow) {
        Assert.notNull(expression, "Expression can't be null");
        if (expression instanceof ColumnExpression) {
            return evaluateColumnExpression((ColumnExpression) expression, dataRow);
        } else if (expression instanceof FunctionExpression) {
            return evaluateFunctionExpression((FunctionExpression) expression, dataRow);
        }
        return evaluateConstant(expression);
    }

    private <T extends Comparable<? super T>> T evaluateConstant(Object constant) {
        return cast(constant, constant.getClass());
    }

    private <T extends Comparable<? super T>> T evaluateColumnExpression(ColumnExpression columnExpression, DataRow dataRow) {
        String tableName = columnExpression.getTableName();
        Optional<TableName> tableNameCandidate = TableName.fromString(tableName);
        if (!tableNameCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format("Table \"%s\" does not exist", tableName));
        }
        String columnName = columnExpression.getColumnName();
        Optional<Column> columnCandidate = tablesAware.getColumn(tableNameCandidate.get(), columnName);
        if (!columnCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format("Table \"%s\" column \"%s\" does not exist", tableName, columnName));
        }
        Column column = columnCandidate.get();
        Class<?> columnType = column.getType();
        Object value = dataRow.get(columnName);
        if (value == null && column.getDefaultValue() != null) {
            value = column.getDefaultValue();
        }
        if (value == null) {
            throw new IllegalArgumentException(String.format("Data row does not contain column \"%s\" and no default value is defined", columnName));
        }
        return cast(value, columnType);
    }
    
    private <T extends Comparable<? super T>> T evaluateFunctionExpression(FunctionExpression functionExpression, DataRow dataRow) {
        String functionName = functionExpression.getFunctionName();
        Optional<Function<?>> functionCandidate = functionsAware.getFunction(functionName);
        if (!functionCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format("Function %s does not exist", functionName));
        }
        List<Object> passedArgs = functionExpression.getArgs(); //This one can contain expressions, so we try to evaluate them
        List<Object> realArgs = passedArgs.stream()
                .map(a -> evaluate(a, dataRow))
                .collect(Collectors.toList());
        Function<?> function = functionCandidate.get();
        Set<String> errors = function.validateInput(realArgs);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.stream().collect(Collectors.joining("; ")));
        }
        Object result = function.apply(realArgs);
        return cast(result, function.getReturnType());
    }
    
    private <T extends Comparable<? super T>> T cast(Object value, Class<?> columnType) {
        if (!Comparable.class.isAssignableFrom(columnType)) {
            throw new IllegalArgumentException(String.format("Column type \"%s\" should implement java.lang.Comparable", columnType.getCanonicalName()));
        }
        @SuppressWarnings("unchecked")
        Class<T> typedColumnType = (Class<T>) columnType;
        return typedColumnType.cast(value);
    }
    
}
