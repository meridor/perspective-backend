package org.meridor.perspective.sql.impl.expression;

public class BinaryArithmeticExpression {
    
    private final Object left;
    
    private final BinaryArithmeticOperator binaryArithmeticOperator;
    
    private final Object right;

    public BinaryArithmeticExpression(Object left, BinaryArithmeticOperator binaryArithmeticOperator, Object right) {
        this.left = left;
        this.binaryArithmeticOperator = binaryArithmeticOperator;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public BinaryArithmeticOperator getBinaryArithmeticOperator() {
        return binaryArithmeticOperator;
    }

    public Object getRight() {
        return right;
    }
    
    @Override
    public boolean equals(Object o) {
        return
                o instanceof BinaryArithmeticExpression &&
                left.equals(((BinaryArithmeticExpression) o).getLeft()) &&
                binaryArithmeticOperator.equals(((BinaryArithmeticExpression) o).getBinaryArithmeticOperator()) &&
                right.equals(((BinaryArithmeticExpression) o).getRight());
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, binaryArithmeticOperator.getText(), right);
    }
}
