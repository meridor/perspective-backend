package org.meridor.perspective.sql;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JoinClause extends BaseQueryPart {

    private final QueryPart previousQueryPart;

    public JoinClause(QueryPart previousQueryPart, JoinType joinType) {
        this.previousQueryPart = previousQueryPart;
        switch (joinType) {
            case INNER: {
                addToSql(" inner join");
                break;
            }
            case LEFT: {
                addToSql(" left join");
                break;
            }
        }
    }
    
    public JoinClause table(String name) {
        addToSql(" " + name);
        return this;
    }
    
    public JoinClause table(String name, String alias) {
        addToSql(String.format(" %s as %s", name, alias));
        return this;
    }
    
    public JoinClause on() {
        addToSql(" on");
        return this;
    }

    public JoinClause equal(String leftColumnName, String rightColumnName) {
        addToSql(String.format(" %s = %s", leftColumnName, rightColumnName));
        return this;
    }

    public JoinClause and() {
        addToSql(" and");
        return this;
    }

    public JoinClause and(Map<String, String> leftRightPairs) {
        Map<String, Collection<String>> joinPairs = leftRightPairs.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        l -> Collections.singletonList(leftRightPairs.get(l))
                ));
        return joinWith(joinPairs, this::equal, this::and, this);
    }
    
    public JoinClause innerJoin() {
        return new JoinClause(this, JoinType.INNER);
    }
    
    public WhereClause where() {
        return new WhereClause(this);
    }

    @Override
    public Optional<QueryPart> getPreviousPart() {
        return Optional.of(previousQueryPart);
    }

    public enum JoinType {
        INNER,
        LEFT
    }
}
