package org.meridor.perspective.sql.impl.expression;

public enum BinaryArithmeticOperator {

    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MOD("%"),
    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("^"),
    SHIFT_LEFT("<<"),
    SHIFT_RIGHT(">>");
    
    private final String text;

    BinaryArithmeticOperator(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
