package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.sql.DataRow;

public interface ExpressionEvaluator {
    
    <T extends Comparable<? super T>> T evaluate(Object expression, DataRow dataRow);

}
