package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.IntStream;

public class ColumnRelation {
    
    private ColumnRelation nextRelation;
    
    private BinaryBooleanOperator joinOperator = BinaryBooleanOperator.AND;
    
    private final String leftTableAlias;
    private final List<String> leftColumns = new ArrayList<>();
    
    private final String rightTableAlias;
    private final List<String> rightColumns = new ArrayList<>();

    public ColumnRelation(String leftTableAlias, List<String> leftColumns, String rightTableAlias, List<String> rightColumns) {
        Assert.isTrue(leftTableAlias != null);
        Assert.isTrue(rightTableAlias != null);
        this.leftTableAlias = leftTableAlias;
        this.leftColumns.addAll(leftColumns);
        this.rightTableAlias = rightTableAlias;
        this.rightColumns.addAll(rightColumns);
    }

    public Optional<ColumnRelation> getNextRelation() {
        return Optional.ofNullable(nextRelation);
    }

    public void setNextRelation(ColumnRelation nextRelation) {
        this.nextRelation = nextRelation;
    }

    public void setJoinOperator(BinaryBooleanOperator joinOperator) {
        this.joinOperator = joinOperator;
    }

    public BinaryBooleanOperator getJoinOperator() {
        return joinOperator;
    }

    public List<String> getTableAliases() {
        return Arrays.asList(leftTableAlias, rightTableAlias);
    }
    
    public Map<String, Set<String>> toMap() {
        Map<String, Set<String>> ret = new HashMap<String, Set<String>>(){
            {
                put(leftTableAlias, new LinkedHashSet<>(leftColumns));
                put(rightTableAlias, new LinkedHashSet<>(rightColumns));
            }
        };

        Optional<ColumnRelation> nextRelationCandidate = getNextRelation();
        if (nextRelationCandidate.isPresent()) {
            Map<String, Set<String>> nextRelationMap = nextRelationCandidate.get().toMap();
            nextRelationMap.keySet().forEach(ta -> {
                Set<String> columns = nextRelationMap.get(ta);
                ret.putIfAbsent(ta, new LinkedHashSet<>());
                ret.get(ta).addAll(columns);
            });
        }
        
        return ret;
    }
    
    public BooleanExpression toBooleanExpression() {
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
        Assert.isTrue(NUM_COLUMNS >= 1, "Column relations should contain at least one column for each table");

        BooleanExpression currentBooleanExpression = IntStream
                .rangeClosed(0, NUM_COLUMNS - 1)
                .mapToObj(index -> (BooleanExpression) new SimpleBooleanExpression(
                        new ColumnExpression(leftColumns.get(index), leftTableAlias),
                        BooleanRelation.EQUAL,
                        new ColumnExpression(rightColumns.get(index), rightTableAlias)
                ))
                .reduce((l, r) -> new BinaryBooleanExpression(l, BinaryBooleanOperator.AND, r)).get();

        Optional<ColumnRelation> nextRelationCandidate = getNextRelation();
        if (nextRelationCandidate.isPresent()) {
            BooleanExpression nextRelationBooleanExpression = nextRelationCandidate.get().toBooleanExpression();
            return new BinaryBooleanExpression(currentBooleanExpression, getJoinOperator(), nextRelationBooleanExpression);
        }
        return currentBooleanExpression;
    }
}
