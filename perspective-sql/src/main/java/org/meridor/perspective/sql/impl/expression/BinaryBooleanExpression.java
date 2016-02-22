package org.meridor.perspective.sql.impl.expression;

public class BinaryBooleanExpression {
    
    private final Object left;
    private final BinaryBooleanOperator binaryBooleanOperator;
    private final Object right;

    public BinaryBooleanExpression(Object left, BinaryBooleanOperator binaryBooleanOperator, Object right) {
        this.left = left;
        this.binaryBooleanOperator = binaryBooleanOperator;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public BinaryBooleanOperator getBinaryBooleanOperator() {
        return binaryBooleanOperator;
    }

    public Object getRight() {
        return right;
    }
    
    public static BinaryBooleanExpression alwaysTrue() {
        return new BinaryBooleanExpression(true, BinaryBooleanOperator.OR, true);
    }
}
