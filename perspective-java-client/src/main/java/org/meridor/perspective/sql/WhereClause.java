package org.meridor.perspective.sql;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.meridor.perspective.sql.PatternUtils.*;

public class WhereClause extends BaseQueryPart {
    
    private final QueryPart previousQueryPart;

    public WhereClause(QueryPart previousQueryPart) {
        this.previousQueryPart = previousQueryPart;
        addToSql(" where");
    }

    public WhereClause equal(String columnName, String value) {
        Parameter parameter = addParameter(columnName, value);
        addToSql(String.format(" %s = :%s:", columnName, parameter.getName()));
        return this;
    }
    
    public WhereClause regexp(String columnName, String value) {
        Parameter parameter = addParameter(columnName, value);
        addToSql(String.format(" %s regexp :%s:", columnName, parameter.getName()));
        return this;
    }
    
    public WhereClause like(String columnName, String value) {
        Parameter parameter = addParameter(columnName, value);
        addToSql(String.format(" %s like :%s:", columnName, parameter.getName()));
        return this;
    }
    
    public WhereClause in(String columnName, Collection<String> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Values can't be empty");
        }
        String joinedParameterNames = values.stream()
                .map(v -> String.format(":%s:", addParameter(columnName, v).getName()))
                .collect(Collectors.joining(", "));
        addToSql(String.format(" %s in (%s)", columnName, joinedParameterNames));
        return this;
    }
    
    public WhereClause isNull(String columnName) {
        addToSql(String.format(" %s is null", columnName));
        return this;
    }
    
    public WhereClause matches(String columnName, String expression) {
        if (expression == null) {
            return isNull(columnName);
        }
        if (isExactMatch(expression)) {
            return equal(columnName, removeFirstAndLastChars(expression));
        }
        if (isContainsMatch(expression)) {
            return like(columnName, expression);
        }
        return regexp(columnName, expression);
    }

    public WhereClause and() {
        addToSql(" and");
        return this;
    }

    public WhereClause matches(Map<String, Collection<String>> columnValues) {
        return joinWith(columnValues, this::matches, this::and, this::or, this);
    }

    public WhereClause or() {
        addToSql(" or");
        return this;
    }

    public OrderClause orderBy() {
        return new OrderClause(this);
    }
    
    public LimitClause limit(int limit) {
        return new LimitClause(this, limit);
    }
    
    public LimitClause limit(int limit, int offset) {
        return new LimitClause(this, limit, offset);
    }
    
    @Override
    public Optional<QueryPart> getPreviousPart() {
        return Optional.of(previousQueryPart);
    }
}
