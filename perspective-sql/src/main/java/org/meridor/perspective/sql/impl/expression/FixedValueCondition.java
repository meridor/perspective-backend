package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.OR;

public class FixedValueCondition extends BaseOptimizationUnit {
    
    private final String tableAlias;
    
    private final String columnName;
    
    private final BooleanRelation booleanRelation;
    
    private final Set<Object> values;

    public FixedValueCondition(String tableAlias, String columnName, BooleanRelation booleanRelation, Set<Object> values) {
        this.tableAlias = tableAlias;
        this.columnName = columnName;
        this.booleanRelation = booleanRelation;
        Assert.isTrue(!values.isEmpty(), "Column values can't be empty on this stage");
        this.values = values;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getColumnName() {
        return columnName;
    }

    public BooleanRelation getBooleanRelation() {
        return booleanRelation;
    }

    public Set<Object> getValues() {
        return values;
    }

    @Override
    protected BooleanExpression getCurrentBooleanExpression() {
        return values.stream()
                .map(v -> (BooleanExpression) new SimpleBooleanExpression(new ColumnExpression(columnName, tableAlias), BooleanRelation.EQUAL, v))
                .reduce((l, r) -> new BinaryBooleanExpression(l, OR, r)).get();
    }

    @Override
    public Set<String> getColumnNames() {
        return Collections.singleton(columnName);
    }

    @Override
    protected String getCurrentString() {
        return values.stream().map(
                v -> String.format("%s.%s %s %s", tableAlias, columnName, booleanRelation.getText(), String.valueOf(v))
        ).collect(Collectors.joining(" OR "));
    }

}
