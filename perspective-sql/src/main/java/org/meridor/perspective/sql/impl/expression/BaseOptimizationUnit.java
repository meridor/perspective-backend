package org.meridor.perspective.sql.impl.expression;

import java.util.Optional;

public abstract class BaseOptimizationUnit implements OptimizationUnit {

    private OptimizationUnit nextUnit;
    
    private BinaryBooleanOperator joinOperator;
    
    @Override
    public Optional<OptimizationUnit> getNextUnit() {
        return Optional.ofNullable(nextUnit);
    }

    public void setNextUnit(OptimizationUnit nextUnit) {
        this.nextUnit = nextUnit;
    }

    @Override
    public BinaryBooleanOperator getJoinOperator() {
        return joinOperator;
    }

    public void setJoinOperator(BinaryBooleanOperator joinOperator) {
        this.joinOperator = joinOperator;
    }

    @Override
    public BooleanExpression toBooleanExpression() {
        BooleanExpression currentBooleanExpression = getCurrentBooleanExpression();
        Optional<OptimizationUnit> nextOptimizationUnit = getNextUnit();
        if (nextOptimizationUnit.isPresent()) {
            BooleanExpression nextRelationBooleanExpression = nextOptimizationUnit.get().toBooleanExpression();
            return new BinaryBooleanExpression(currentBooleanExpression, getJoinOperator(), nextRelationBooleanExpression);
        }
        return currentBooleanExpression;
    }

    protected abstract BooleanExpression getCurrentBooleanExpression();

    @Override
    public String toString() {
        String currentString = getCurrentString();
        if (getJoinOperator() != null) {
            currentString = String.format(" %s %s", getJoinOperator().getText(), currentString);
        }
        Optional<OptimizationUnit> nextUnitCandidate = getNextUnit();
        return nextUnitCandidate.isPresent() ?
                currentString + nextUnitCandidate.get().toString() :
                currentString;

    }
    
    protected abstract String getCurrentString();
}
