package org.meridor.perspective.sql.impl.expression;

public enum BinaryBooleanOperator {
    
    AND("&&"),
    OR("||"),
    XOR("^");
    
    private final String text;

    BinaryBooleanOperator(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
