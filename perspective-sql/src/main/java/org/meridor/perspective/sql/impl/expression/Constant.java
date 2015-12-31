package org.meridor.perspective.sql.impl.expression;

public class Constant {
    
    private final Object value;

    public Constant(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
