package org.meridor.perspective.sql.impl.parser;

import org.meridor.perspective.sql.impl.expression.OrderExpression;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SelectQueryAware {

    Map<String, Object> getSelectionMap();
    Optional<Object> getWhereExpression();
    Optional<Object> getHavingExpression();
    List<Object> getGroupByExpressions();
    List<OrderExpression> getOrderByExpressions();
    Optional<Integer> getLimitCount();
    Optional<Integer> getLimitOffset();
}
