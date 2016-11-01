package org.meridor.perspective.sql.impl.expression;

import java.util.Optional;
import java.util.Set;

public interface OptimizationUnit {

    BooleanExpression toBooleanExpression();

    Optional<OptimizationUnit> getNextUnit();

    BinaryBooleanOperator getJoinOperator();
    
    Set<String> getColumnNames();
    
}
