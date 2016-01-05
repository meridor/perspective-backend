package org.meridor.perspective.sql.impl.expression;

public class ComplexBooleanExpression {
    
    private final Object left;
    private final BooleanOperation booleanOperation;
    private final Object right;

    public ComplexBooleanExpression(Object left, BooleanOperation booleanOperation, Object right) {
        this.left = left;
        this.booleanOperation = booleanOperation;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public BooleanOperation getBooleanOperation() {
        return booleanOperation;
    }

    public Object getRight() {
        return right;
    }
}
