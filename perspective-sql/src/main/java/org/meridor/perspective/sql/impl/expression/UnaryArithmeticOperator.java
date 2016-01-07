package org.meridor.perspective.sql.impl.expression;

public enum UnaryArithmeticOperator {

    PLUS("+"),
    MINUS("-"),
    BIT_NOT("~");
    
    private final String text;

    UnaryArithmeticOperator(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
