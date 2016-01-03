package org.meridor.perspective.sql.impl.expression;

public class ComplexBooleanExpression {
    
    private final SimpleBooleanExpression left;
    private final BooleanOperation booleanOperation;
    private final SimpleBooleanExpression right;

    public ComplexBooleanExpression(SimpleBooleanExpression left, BooleanOperation booleanOperation, SimpleBooleanExpression right) {
        this.left = left;
        this.booleanOperation = booleanOperation;
        this.right = right;
    }

    public SimpleBooleanExpression getLeft() {
        return left;
    }

    public BooleanOperation getBooleanOperation() {
        return booleanOperation;
    }

    public SimpleBooleanExpression getRight() {
        return right;
    }
}
