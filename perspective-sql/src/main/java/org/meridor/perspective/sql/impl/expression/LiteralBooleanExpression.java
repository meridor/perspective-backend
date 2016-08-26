package org.meridor.perspective.sql.impl.expression;

public class LiteralBooleanExpression implements BooleanExpression {
    
    private final boolean literal;

    public LiteralBooleanExpression(boolean literal) {
        this.literal = literal;
    }

    public boolean getLiteral() {
        return literal;
    }

    @Override
    public boolean equals(Object another) {
        return 
                another instanceof LiteralBooleanExpression
                && literal == ((LiteralBooleanExpression) another).getLiteral();
    }

    @Override
    public String toString() {
        return "LiteralBooleanExpression{" +
                "literal=" + literal +
                '}';
    }
}
