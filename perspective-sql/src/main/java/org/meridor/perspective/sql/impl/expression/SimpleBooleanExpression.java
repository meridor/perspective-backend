package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;

import java.util.*;

import static org.meridor.perspective.beans.BooleanRelation.EQUAL;
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
        //Current we support only equal conditions but should also support >, < and so on
        if (booleanRelation == EQUAL) {
            if (isColumnExpression(left) && isConstant(right)) {
                ColumnExpression leftColumnExpression = asColumnExpression(left);
                String leftTableAlias = leftColumnExpression.getTableAlias();
                String leftColumnName = leftColumnExpression.getColumnName();
                if (leftTableAlias.equals(tableAlias)) {
                    ret.put(leftColumnName, Collections.singleton(right));
                }
            }
            if (isConstant(left) && isColumnExpression(right)) {
                ColumnExpression rightColumnExpression = asColumnExpression(right);
                String rightTableAlias = rightColumnExpression.getTableAlias();
                String rightColumnName = rightColumnExpression.getColumnName();
                if (rightTableAlias.equals(tableAlias)) {
                    ret.put(rightColumnName, Collections.singleton(left));
                }
            }
        }
        return ret;
    }

    @Override
    public List<ColumnRelation> getColumnRelations() {
        if (isColumnExpression(left) && isColumnExpression(right)) {
            String leftTableAlias = asColumnExpression(left).getTableAlias();
            String leftColumnName = asColumnExpression(left).getColumnName();
            String rightTableAlias = asColumnExpression(right).getTableAlias();
            String rightColumnName = asColumnExpression(right).getColumnName();
            return Collections.singletonList(new ColumnRelation(
                    leftTableAlias,
                    leftColumnName,
                    rightTableAlias,
                    rightColumnName
            ));
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<BooleanExpression> getRestOfExpression() {
        return (
                isConstant(left) && isConstant(right) ||
                canNotOptimizeExpression(left) ||
                canNotOptimizeExpression(right)
        ) ? Optional.of(this) : Optional.empty();
    }
    
    private boolean canNotOptimizeExpression(Object value) {
        return 
                (booleanRelation != EQUAL) || 
                ( !isConstant(value) && !isColumnExpression(value) );
    }
}
