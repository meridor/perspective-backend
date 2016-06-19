package org.meridor.perspective.sql.impl.expression;

public class UnaryArithmeticExpression {
    
    private final Object value;
    
    private final UnaryArithmeticOperator unaryArithmeticOperator;

    public UnaryArithmeticExpression(Object value, UnaryArithmeticOperator unaryArithmeticOperator) {
        this.value = value;
        this.unaryArithmeticOperator = unaryArithmeticOperator;
    }

    public Object getValue() {
        return value;
    }

    public UnaryArithmeticOperator getUnaryArithmeticOperator() {
        return unaryArithmeticOperator;
    }

    @Override
    public String toString() {
        return unaryArithmeticOperator.getText() + value;
    }
}
