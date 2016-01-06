package org.meridor.perspective.sql.impl.expression;

public class UnaryBooleanExpression {
    
    private final Object value;
    
    private final UnaryBooleanOperator unaryBooleanOperator;

    public UnaryBooleanExpression(Object value, UnaryBooleanOperator unaryBooleanOperator) {
        this.value = value;
        this.unaryBooleanOperator = unaryBooleanOperator;
    }

    public Object getValue() {
        return value;
    }

    public UnaryBooleanOperator getUnaryBooleanOperator() {
        return unaryBooleanOperator;
    }
}
