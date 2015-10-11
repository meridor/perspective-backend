package org.meridor.perspective.shell.validator;

public enum NumberRelation {
    
    EQUAL("="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<=");
    
    private final String sign;

    NumberRelation(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }
}
