package org.meridor.perspective.beans;

public enum BooleanRelation {
    
    EQUAL("="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<="),
    NOT_EQUAL("!="),
    LIKE("LIKE"),
    REGEXP("REGEXP");
    
    private final String sign;

    BooleanRelation(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }
}
