package org.meridor.perspective.sql.impl.parser;

import org.meridor.perspective.sql.impl.expression.BooleanExpression;
import org.meridor.perspective.sql.impl.expression.OrderExpression;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SelectQueryAware {

    /**
     * Which columns and expressions should be selected
     * @return a map of alias to expression
     */
    Map<String, Object> getSelectionMap();

    /**
     * Returns data selection tree that describes tables and joins
     * @return a datasource object or empty
     */
    Optional<DataSource> getDataSource();

    /**
     * Returns how aliases are mapped to real table names
     * @return a map of alias to table name
     */
    Map<String, String> getTableAliases();
    
    /**
     * Which columns are available for selection after all joins
     * @return a map of column name to aliases
     */
    Map<String, List<String>> getAvailableColumns();

    /**
     * Returns where expression to apply after all joins
     * @return boolean where expression or empty
     */
    Optional<BooleanExpression> getWhereExpression();

    /**
     * Returns a list of expressions to group by
     * @return a list of expressions to group by
     */
    List<Object> getGroupByExpressions();
    
    /**
     * Return a having expression to apply after grouping
     * @return boolean having expression or empty
     */
    Optional<BooleanExpression> getHavingExpression();

    /**
     * Returns a list of expressions for order by clause
     * @return a list of expressions
     */
    List<OrderExpression> getOrderByExpressions();

    /**
     * Returns limit count
     * @return limit count or empty
     */
    Optional<Integer> getLimitCount();

    /**
     * Returns limit offset
     * @return limit offset or empty
     */
    Optional<Integer> getLimitOffset();
}
