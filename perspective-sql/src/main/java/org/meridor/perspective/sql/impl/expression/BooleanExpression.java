package org.meridor.perspective.sql.impl.expression;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BooleanExpression {

    /**
     * Returns all table aliases present in expression
     */
    Set<String> getTableAliases();

    /**
     * Returns conditions like table.column = 'value' 
     */
    Map<String, Set<Object>> getFixedValueConditions(String tableAlias);

    /**
     * Returns conditions like table1.column1 = table2.column2 
     */
    Optional<ColumnRelation> getColumnRelations();

    /**
     * Returns the rest of expression that can't be classified 
     * @return boolean expression or empty
     */
    Optional<BooleanExpression> getRestOfExpression();
    
}
