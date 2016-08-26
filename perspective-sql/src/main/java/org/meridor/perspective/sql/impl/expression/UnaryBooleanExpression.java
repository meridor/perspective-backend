package org.meridor.perspective.sql.impl.expression;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UnaryBooleanExpression implements BooleanExpression {
    
    private final BooleanExpression value;
    
    private final UnaryBooleanOperator unaryBooleanOperator;

    public UnaryBooleanExpression(BooleanExpression value, UnaryBooleanOperator unaryBooleanOperator) {
        this.value = value;
        this.unaryBooleanOperator = unaryBooleanOperator;
    }

    public BooleanExpression getValue() {
        return value;
    }

    public UnaryBooleanOperator getUnaryBooleanOperator() {
        return unaryBooleanOperator;
    }

    @Override
    public Set<String> getTableAliases() {
        return value.getTableAliases();
    }

    @Override
    public Map<String, Set<Object>> getFixedValueConditions(String tableAlias) {
        //TODO: !(expr) optimization will be implemented later...
        return Collections.emptyMap();
    }

    @Override
    public Optional<BooleanExpression> getRestOfExpression() {
        return Optional.of(this);
    }

    @Override
    public boolean equals(Object another) {
        return 
                another instanceof UnaryBooleanExpression
                && value.equals(((UnaryBooleanExpression) another).getValue())
                && unaryBooleanOperator.equals(((UnaryBooleanExpression) another).getUnaryBooleanOperator());
    }

    @Override
    public String toString() {
        return unaryBooleanOperator.getText() + " " + value;
    }
}
