package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.beans.BooleanRelation;

public class SimpleBooleanExpression {
    
    private final Object left;
    private final BooleanRelation booleanRelation;
    private final Object right;

    public SimpleBooleanExpression(Object left, BooleanRelation booleanRelation, Object right) {
        this.left = left;
        this.booleanRelation = booleanRelation;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public BooleanRelation getBooleanRelation() {
        return booleanRelation;
    }

    public Object getRight() {
        return right;
    }
}
