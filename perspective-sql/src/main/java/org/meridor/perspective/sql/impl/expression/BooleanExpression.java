package org.meridor.perspective.sql.impl.expression;

import java.util.*;

public interface BooleanExpression {

    /**
     * Returns all table aliases present in expression
     */
    default Set<String> getTableAliases(){
        return Collections.emptySet();
    }

    /**
     * Returns conditions like table.column = 'value' 
     */
    default Map<String, Set<Object>> getFixedValueConditions(String tableAlias) {
        return Collections.emptyMap();
    }

    /**
     * Returns conditions like table1.column1 = table2.column2
     * Each condition in the list is independent from the others i.e. does not
     * contain OR and XOR operators. This allows to move such condition back and
     * forth from joins to where clause depending on existing indexes.
     */
    default List<ColumnRelation> getColumnRelations() {
        return Collections.emptyList();
    }

    /**
     * Returns the rest of expression that can't be classified 
     * @return boolean expression or empty
     */
    default Optional<BooleanExpression> getRestOfExpression() {
        return Optional.empty();
    }
    
}
