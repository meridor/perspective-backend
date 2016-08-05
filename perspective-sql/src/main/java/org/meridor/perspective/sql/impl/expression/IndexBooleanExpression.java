package org.meridor.perspective.sql.impl.expression;

import java.util.*;

/**
 * This type of expression is used to pass optimized conditions to {@link org.meridor.perspective.sql.impl.task.strategy.IndexScanStrategy}.
 * It contains only conditions corresponding to one table.
 */
public class IndexBooleanExpression implements BooleanExpression {

    private final Map<String, Set<Object>> fixedValueConditions = new HashMap<>();
    
    private final List<ColumnRelation> columnRelations = new ArrayList<>();

    public IndexBooleanExpression(Map<String, Set<Object>> fixedValueConditions) {
        this.fixedValueConditions.putAll(fixedValueConditions);
    }

    public IndexBooleanExpression() {
    }

    @Override
    public Map<String, Set<Object>> getFixedValueConditions(String tableAlias) {
        return new HashMap<>(fixedValueConditions);
    }

    @Override
    public List<ColumnRelation> getColumnRelations() {
        return columnRelations;
    }
    
    @Override
    public String toString() {
        return "IndexBooleanExpression{" +
                ", fixedValueConditions=" + fixedValueConditions +
                ", columnRelations=" + columnRelations +
                '}';
    }
}
