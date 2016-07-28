package org.meridor.perspective.sql.impl.expression;

import java.util.*;

/**
 * This type of expression is used to pass optimized conditions to {@link org.meridor.perspective.sql.impl.task.strategy.IndexScanStrategy}.
 * It contains only conditions corresponding to one table.
 */
public class IndexBooleanExpression implements BooleanExpression {

    private final String tableAlias;
    
    private final Map<String, Set<Object>> fixedValueConditions = new HashMap<>();

    public IndexBooleanExpression(String tableAlias, Map<String, Set<Object>> fixedValueConditions) {
        this.tableAlias = tableAlias;
        this.fixedValueConditions.putAll(fixedValueConditions);
    }

    public static IndexBooleanExpression empty() {
        return new IndexBooleanExpression(null, Collections.emptyMap());
    }

    @Override
    public Set<String> getTableAliases() {
        return tableAlias != null ? Collections.singleton(tableAlias) : Collections.emptySet();
    }

    @Override
    public Map<String, Set<Object>> getFixedValueConditions(String tableAlias) {
        return new HashMap<>(fixedValueConditions);
    }

    @Override
    public Optional<ColumnRelation> getColumnRelations() {
        return Optional.empty();
    }

    @Override
    public Optional<BooleanExpression> getRestOfExpression() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "IndexBooleanExpression{" +
                "tableAlias='" + tableAlias + '\'' +
                ", fixedValueConditions=" + fixedValueConditions +
                '}';
    }
}
