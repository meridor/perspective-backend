package org.meridor.perspective.sql.impl.parser;

class AliasExpressionPair {

    private final String alias;
    private final Object expression;

    public AliasExpressionPair(String alias, Object expression) {
        this.alias = alias;
        this.expression = expression;
    }

    public static AliasExpressionPair pair(String str, Object o) {
        return new AliasExpressionPair(str, o);
    }
    
    public static AliasExpressionPair emptyPair() {
        return new AliasExpressionPair("", new Object());
    }

    public String getAlias() {
        return alias;
    }

    public Object getExpression() {
        return expression;
    }
}
