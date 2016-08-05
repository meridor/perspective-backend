package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;
import org.springframework.util.Assert;

import java.util.*;

public class ColumnRelation {
    
    private ColumnRelation nextRelation;
    
    private BinaryBooleanOperator joinOperator = BinaryBooleanOperator.AND;
    
    private final String leftTableAlias;
    private final String leftColumn;
    
    private final String rightTableAlias;
    private final String rightColumn;

    public ColumnRelation(String leftTableAlias, String leftColumn, String rightTableAlias, String rightColumn) {
        Assert.isTrue(leftTableAlias != null);
        Assert.isTrue(rightTableAlias != null);
        this.leftTableAlias = leftTableAlias;
        this.leftColumn = leftColumn;
        this.rightTableAlias = rightTableAlias;
        this.rightColumn = rightColumn;
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

    public String getLeftTableAlias() {
        return leftTableAlias;
    }

    public String getRightTableAlias() {
        return rightTableAlias;
    }

    public String getLeftColumn() {
        return leftColumn;
    }

    public String getRightColumn() {
        return rightColumn;
    }

    public String getColumnName(String tableAlias) {
        if (getLeftTableAlias().equals(tableAlias)) {
            return getLeftColumn();
        }
        if (getRightTableAlias().equals(tableAlias)) {
            return getRightColumn();
        }
        throw new IllegalArgumentException(String.format("Table alias %s not found in column relation %s", tableAlias, this));
    }
    
    public Map<String, Set<String>> toMap() {
        return new HashMap<String, Set<String>>(){
            {
                put(leftTableAlias, new LinkedHashSet<>(Collections.singletonList(leftColumn)));
                put(rightTableAlias, new LinkedHashSet<>(Collections.singletonList(rightColumn)));
            }
        };
    }
    
    public BooleanExpression toBooleanExpression() {
        BooleanExpression currentBooleanExpression = 
                new SimpleBooleanExpression(
                        new ColumnExpression(leftColumn, leftTableAlias),
                        BooleanRelation.EQUAL,
                        new ColumnExpression(rightColumn, rightTableAlias)
                );

        Optional<ColumnRelation> nextRelationCandidate = getNextRelation();
        if (nextRelationCandidate.isPresent()) {
            BooleanExpression nextRelationBooleanExpression = nextRelationCandidate.get().toBooleanExpression();
            return new BinaryBooleanExpression(currentBooleanExpression, getJoinOperator(), nextRelationBooleanExpression);
        }
        return currentBooleanExpression;
    }
    
    //Returns a chain of column relations as list
    public List<ColumnRelation> toList() {
        List<ColumnRelation> ret = new ArrayList<>();
        ret.add(this);
        if (getNextRelation().isPresent()) {
            ret.addAll(getNextRelation().get().toList());
        }
        return ret;
    }

    @Override
    public String toString() {
        return String.format(
                "ColumnRelation{%s.%s = %s.%s}",
                leftTableAlias, leftColumn, rightTableAlias, rightColumn
        );
    }
}
