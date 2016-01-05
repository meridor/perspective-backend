package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;
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
import java.util.regex.Pattern;
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
        if (expression instanceof Null) {
            return null;
        } else if (expression instanceof ColumnExpression) {
            return evaluateColumnExpression((ColumnExpression) expression, dataRow);
        } else if (expression instanceof FunctionExpression) {
            return evaluateFunctionExpression((FunctionExpression) expression, dataRow);
        } else if (expression instanceof SimpleBooleanExpression) {
            return cast(evaluateSimpleBooleanExpression((SimpleBooleanExpression) expression, dataRow), Boolean.class, Comparable.class);
        } else if (expression instanceof ComplexBooleanExpression) {
            return cast(evaluateComplexBooleanExpression((ComplexBooleanExpression) expression, dataRow), Boolean.class, Comparable.class);
        }
        return evaluateConstant(expression);
    }

    @Override
    public <T extends Comparable<? super T>> T evaluateAs(Object expression, DataRow dataRow, Class<T> cls) {
        Object result = evaluate(expression, dataRow);
        return result != null ? cast(result, result.getClass(), cls): null;
    }
    
    private <T extends Comparable<? super T>> T  evaluateAsOrDefault(Object expression, DataRow dataRow, Class<T> cls, T defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        T result = evaluateAs(expression, dataRow, cls);
        return (result != null) ? result : defaultValue; 
    }

    private <T extends Comparable<? super T>> T evaluateConstant(Object expression) {
        Class<?> expressionClass = expression.getClass();
        if (isConstant(expressionClass)) {
            return cast(expression, expressionClass, Comparable.class);
        }
        throw new IllegalArgumentException(String.format("Constant should be a string or a number but %s was given", expressionClass.getCanonicalName()));
    }

    private boolean isConstant(Class<?> expressionClass) {
        return Number.class.isAssignableFrom(expressionClass) || String.class.isAssignableFrom(expressionClass);
    }
    
    private boolean isString(Class<?> expressionClass) {
        return String.class.isAssignableFrom(expressionClass);
    }
    
    private boolean isInteger(Class<?> expressionClass) {
        return Integer.class.isAssignableFrom(expressionClass) || Long.class.isAssignableFrom(expressionClass);
    }
    
    private boolean isDouble(Class<?> expressionClass) {
        return Float.class.isAssignableFrom(expressionClass) || Double.class.isAssignableFrom(expressionClass);
    }
    
    private boolean isNumber(Class<?> expressionClass) {
        return isInteger(expressionClass) || isDouble(expressionClass);
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
        return cast(value, columnType, Comparable.class);
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
        return cast(result, function.getReturnType(), Comparable.class);
    }
    
    private boolean evaluateSimpleBooleanExpression(SimpleBooleanExpression simpleBooleanExpression, DataRow dataRow) {
        if (simpleBooleanExpression == null) {
            return false;
        }
        Object left = simpleBooleanExpression.getLeft();
        Object right = simpleBooleanExpression.getRight();
        BooleanRelation booleanRelation = simpleBooleanExpression.getBooleanRelation();
        if (left == null || right == null) {
            return false;
        }
        if (!isConstant(left.getClass())) {
            return evaluateSimpleBooleanExpression(new SimpleBooleanExpression(evaluate(left, dataRow), booleanRelation, right), dataRow);
        }
        if (!isConstant(right.getClass())) {
            return evaluateSimpleBooleanExpression(new SimpleBooleanExpression(left, booleanRelation, evaluate(right, dataRow)), dataRow);
        }
        Class<?> leftClass = left.getClass();
        Class<?> rightClass = right.getClass();
        if (isString(leftClass) && isString(rightClass)) {
            String leftAsString = String.valueOf(left);
            String rightAsString = String.valueOf(right);
            switch (booleanRelation) {
                case EQUAL: return leftAsString.equals(rightAsString);
                case NOT_EQUAL: return !leftAsString.equals(rightAsString);
                case LIKE: {
                    String rightAsRegex = rightAsString
                            .replace("%", ".*")
                            .replace("\\%", "%")
                            .replace("_", ".")
                            .replace("\\_", "_");
                    return Pattern.matches(rightAsRegex, leftAsString);
                }
                default: throw new IllegalArgumentException("This operation is not applicable to strings");
            }
        } else if (isNumber(leftClass) && isNumber(rightClass)) {
            double leftAsDouble = Double.valueOf(left.toString());
            double rightAsDouble = Double.valueOf(right.toString());
            switch (booleanRelation) {
                case EQUAL: return leftAsDouble == rightAsDouble; //This one can probably cause rounding problems when comparing integers
                case GREATER_THAN: return leftAsDouble > rightAsDouble;
                case LESS_THAN: return leftAsDouble < rightAsDouble;
                case GREATER_THAN_EQUAL: return leftAsDouble >= rightAsDouble;
                case LESS_THAN_EQUAL: return leftAsDouble <= rightAsDouble;
                case NOT_EQUAL: return leftAsDouble != rightAsDouble;
            }
        }
        throw new IllegalArgumentException(String.format("Incorrect boolean expression argument types: %s and %s", leftClass, rightClass));
    }

    private boolean evaluateComplexBooleanExpression(ComplexBooleanExpression complexBooleanExpression, DataRow dataRow) {
        boolean left = evaluateAsOrDefault(complexBooleanExpression.getLeft(), dataRow, Boolean.class, false);
        boolean right = evaluateAsOrDefault(complexBooleanExpression.getRight(), dataRow, Boolean.class, false);
        switch (complexBooleanExpression.getBooleanOperation()) {
            case NOT: return !left;
            default:
            case AND: return left && right;
            case OR: return left || right;
            case XOR: return left ^ right;
        }
    }
    
    private <T extends Comparable<? super T>> T cast(Object value, Class<?> columnType, Class<?> requiredSuperclass) {
        if (!requiredSuperclass.isAssignableFrom(columnType)) {
            throw new IllegalArgumentException(String.format("Column type \"%s\" should subclass \"%s\"", columnType.getCanonicalName(), requiredSuperclass.getCanonicalName()));
        }
        @SuppressWarnings("unchecked")
        Class<T> typedColumnType = (Class<T>) columnType;
        return typedColumnType.cast(value);
    }
    
}
