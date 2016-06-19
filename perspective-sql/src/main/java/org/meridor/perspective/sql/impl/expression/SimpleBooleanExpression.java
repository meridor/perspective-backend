package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;

import java.util.*;

import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.*;

public class SimpleBooleanExpression implements BooleanExpression {
    
    private final Object left;
    private final BooleanRelation booleanRelation;
    private final Object right;

    public SimpleBooleanExpression(Object left, BooleanRelation booleanRelation, Object right) {
        this.left = left;
        this.booleanRelation = booleanRelation;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public BooleanRelation getBooleanRelation() {
        return booleanRelation;
    }

    public Object getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, booleanRelation.getText(), right);
    }

    @Override
    public Set<String> getTableAliases() {
        Set<String> ret = new LinkedHashSet<>();
        if (isColumnExpression(left)) {
            ret.add(((ColumnExpression) left).getTableAlias());
        }
        if (isColumnExpression(right)) {
            ret.add(((ColumnExpression) right).getTableAlias());
        }
        return ret;
    }

    @Override
    public Map<String, Set<Object>> getFixedValueConditions(String tableAlias) {
        Map<String, Set<Object>> ret = new HashMap<>();
        if (isColumnExpression(left) && isConstant(right)) {
            String leftTableAlias = asColumnExpression(right).getTableAlias();
            if (leftTableAlias.equals(tableAlias)) {
                ret.put(leftTableAlias, Collections.singleton(right));
            }
        }
        if (isConstant(left) && isColumnExpression(right)) {
            String rightTableAlias = asColumnExpression(right).getTableAlias();
            if (rightTableAlias.equals(tableAlias)) {
                ret.put(rightTableAlias, Collections.singleton(left));
            }
        }
        return ret;
    }

    @Override
    public Map<String, Set<String>> getColumnRelations() {
        Map<String, Set<String>> ret = new HashMap<>();
        if (isColumnExpression(left) && isColumnExpression(right)) {
            String leftTableAlias = asColumnExpression(left).getTableAlias();
            String leftColumnName = asColumnExpression(left).getColumnName();
            ret.put(leftTableAlias, Collections.singleton(leftColumnName));
            String rightTableAlias = asColumnExpression(right).getTableAlias();
            String rightColumnName = asColumnExpression(right).getColumnName();
            ret.put(rightTableAlias, Collections.singleton(rightColumnName));
        }
        return ret;
    }

    @Override
    public Optional<BooleanExpression> getRestOfExpression() {
        return (
                isConstant(left) && isConstant(right) ||
                isNotOptimizableExpression(left) ||
                isNotOptimizableExpression(right)
        ) ? Optional.of(this) : Optional.empty();
    }
    
    private boolean isNotOptimizableExpression(Object value) {
        return !isConstant(value) && !isColumnExpression(value);
    }
}
