package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.meridor.perspective.beans.BooleanRelation.EQUAL;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.AND;

public final class ExpressionUtils {

    public static boolean isConstant(Class<?> expressionClass) {
        return
                isNumber(expressionClass) ||
                        isString(expressionClass) ||
                        isDate(expressionClass) ||
                        isBoolean(expressionClass) ||
                        expressionClass.isEnum();
    }

    public static boolean isConstant(Object expression) {
        return expression != null && isConstant(expression.getClass());
    }
    
    public static boolean isString(Class<?> expressionClass) {
        return String.class.isAssignableFrom(expressionClass);
    }

    public static boolean isInteger(Class<?> expressionClass) {
        return Integer.class.isAssignableFrom(expressionClass) || Long.class.isAssignableFrom(expressionClass);
    }

    public static boolean isDouble(Class<?> expressionClass) {
        return Float.class.isAssignableFrom(expressionClass) || Double.class.isAssignableFrom(expressionClass);
    }

    public static boolean isNumber(Class<?> expressionClass) {
        return isInteger(expressionClass) || isDouble(expressionClass);
    }

    public static boolean isBoolean(Class<?> expressionClass) {
        return Boolean.class.isAssignableFrom(expressionClass);
    }

    public static boolean isDate(Class<?> expressionClass) {
        return ZonedDateTime.class.isAssignableFrom(expressionClass);
    }
    
    public static boolean isColumnExpression(Object expression) {
        return expression instanceof ColumnExpression;
    }

    public static boolean oneOfIsNull(Object left, Object right) {
        return left == null || right == null;
    }

    public static boolean bothAreNumbers(Class<?> leftClass, Class<?> rightClass) {
        return isNumber(leftClass) && isNumber(rightClass);
    }

    public static boolean bothAreIntegers(Class<?> leftClass, Class<?> rightClass) {
        return isInteger(leftClass) && isInteger(rightClass);
    }

    public static boolean bothAreStrings(Class<?> leftClass, Class<?> rightClass) {
        return isString(leftClass) && isString(rightClass);
    }

    public static boolean bothAreBooleans(Class<?> leftClass, Class<?> rightClass) {
        return isBoolean(leftClass) && isBoolean(rightClass);
    }

    public static int asInt(Object value) {
        return Integer.valueOf(value.toString());
    }

    public static double asDouble(Object value) {
        return Double.valueOf(value.toString());
    }

    public static String asString(Object value) {
        return String.valueOf(value);
    }

    public static Boolean asBoolean(Object value) {
        return Boolean.valueOf(asString(value));
    }

    public static ColumnExpression asColumnExpression(Object value) {
        return ColumnExpression.class.cast(value);
    }

    public static List<String> columnsToNames(Collection<Column> columns) {
        return columns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
    }

    public static Optional<BooleanExpression> columnsToCondition(Optional<BooleanExpression> joinCondition, String leftTableAlias, List<String> columnNames, String rightTableAlias) {
        if (columnNames.size() == 0) {
            return joinCondition;
        }
        return columnNames.stream()
                        .map(cn -> (BooleanExpression) new SimpleBooleanExpression(
                                new ColumnExpression(cn, leftTableAlias),
                                EQUAL,
                                new ColumnExpression(cn, rightTableAlias)
                        ))
                        .reduce((l, r) -> new BinaryBooleanExpression(l, AND, r));
    }

    public static BooleanExpression columnRelationsToExpression(Map<String, Set<String>> columnRelations) {
        Assert.isTrue(columnRelations.size() == 2, String.format("Column relations should contain 2 tables but contains %d", columnRelations.size()));
        List<String> tableAliases = new ArrayList<>(columnRelations.keySet());
        String leftTableAlias = tableAliases.get(0);
        List<String> leftColumns = new ArrayList<>(columnRelations.get(leftTableAlias));
        String rightTableAlias = tableAliases.get(1);
        List<String> rightColumns = new ArrayList<>(columnRelations.get(rightTableAlias));
        Assert.isTrue(
                leftColumns.size() == rightColumns.size(),
                String.format(
                        "Column relations should have equal number of columns" +
                                " for each table alias but \"%s\" contains" +
                                " %d columns and \"%s\" has %d columns",
                        leftTableAlias,
                        leftColumns.size(),
                        rightTableAlias,
                        rightColumns.size()
                )
        );
        final int NUM_COLUMNS = leftColumns.size();

        return IntStream
                .rangeClosed(0, NUM_COLUMNS - 1)
                .mapToObj(index -> (BooleanExpression) new SimpleBooleanExpression(
                        new ColumnExpression(leftColumns.get(index), leftTableAlias),
                        BooleanRelation.EQUAL,
                        new ColumnExpression(rightColumns.get(index), rightTableAlias)
                ))
                .reduce((l, r) -> new BinaryBooleanExpression(l, BinaryBooleanOperator.AND, r)).get();
    }
    

    private ExpressionUtils() {}
    
}
