package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.sql.DataRow;

import java.util.List;
import java.util.Map;

public interface ExpressionEvaluator {
    
    <T extends Comparable<? super T>> T evaluate(Object expression, DataRow dataRow);
    
    Map<String, List<String>> getColumnNames(Object expression);

}
