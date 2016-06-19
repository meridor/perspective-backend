package org.meridor.perspective.sql.impl.expression;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.isColumnExpression;

public class InExpression implements BooleanExpression {
    
    private final Object value;
    
    private final Set<Object> candidates;

    public InExpression(Object value, Set<Object> candidates) {
        this.value = value;
        this.candidates = candidates;
    }

    public Object getValue() {
        return value;
    }

    public Set<Object> getCandidates() {
        return candidates;
    }

    @Override
    public String toString() {
        return String.format(
                "%s IN (%s)",
                value,
                Stream.of(candidates).map(String::valueOf).collect(Collectors.joining(", "))
        );
    }

    @Override
    public Set<String> getTableAliases() {
        //For the moment we optimize only column expressions
        if (isColumnExpression(value)) {
            return Collections.singleton(((ColumnExpression) value).getTableAlias());
        }
        return Collections.emptySet();
    }

    @Override
    public Map<String, Set<Object>> getFixedValueConditions(String tableAlias) {
        if (value instanceof BooleanExpression) {
            return ((BooleanExpression) value).getFixedValueConditions(tableAlias);
        }
        if (isColumnExpression(value)) {
            String valueTableAlias = ((ColumnExpression) value).getTableAlias();
            String columnName = ((ColumnExpression) value).getColumnName();
            if (tableAlias.equals(valueTableAlias)) {
                return Collections.singletonMap(columnName, getCandidates());
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Set<String>> getColumnRelations() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<BooleanExpression> getRestOfExpression() {
        return isColumnExpression(value) ?
                Optional.empty() :
                Optional.of(this);
    }
}
