package org.meridor.perspective.sql.impl.parser;

class AliasExpressionPair {

    private final Pair<String, Object> pair;
    
    AliasExpressionPair(String alias, Object expression) {
        this.pair = new Pair<>(alias, expression);
    }

    static AliasExpressionPair pair(String str, Object o) {
        return new AliasExpressionPair(str, o);
    }
    
    static AliasExpressionPair emptyPair() {
        return new AliasExpressionPair("", new Object());
    }

    public String getAlias() {
        return pair.getFirst();
    }

    public Object getExpression() {
        return pair.getSecond();
    }
}
