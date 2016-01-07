package org.meridor.perspective.sql.impl.expression;

public enum UnaryBooleanOperator {
    NOT("NOT");
    
    private final String text;

    UnaryBooleanOperator(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
