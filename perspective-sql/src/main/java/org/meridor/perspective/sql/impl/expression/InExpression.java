package org.meridor.perspective.sql.impl.expression;

import java.util.List;

public class InExpression {
    
    private final Object value;
    
    private final List<Object> candidates;

    public InExpression(Object value, List<Object> candidates) {
        this.value = value;
        this.candidates = candidates;
    }

    public Object getValue() {
        return value;
    }

    public List<Object> getCandidates() {
        return candidates;
    }
}
