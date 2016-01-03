package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.sql.DataRow;

import java.util.List;
import java.util.Map;

public interface ExpressionEvaluator {
    
    <T extends Comparable<? super T>> T evaluate(Object expression, DataRow dataRow);
    
    <T extends Comparable<? super T>> T evaluateAs(Object expression, DataRow dataRow, Class<T> cls);
    
    Map<String, List<String>> getColumnNames(Object expression);

}
