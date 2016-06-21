package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.impl.expression.BooleanExpression;
import org.meridor.perspective.sql.impl.expression.OrderExpression;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.SelectQueryAware;

import java.sql.SQLSyntaxErrorException;
import java.util.*;

public class MockSelectQueryAware implements SelectQueryAware {

    private final Map<String, Object> selectionMap = new LinkedHashMap<>();

    private DataSource dataSource;

    private final Map<String, String> tableAliases = new HashMap<>();

    private final Map<String, List<String>> availableColumns = new HashMap<>();

    private BooleanExpression whereExpression;

    private final List<Object> groupByExpressions = new ArrayList<>();

    private BooleanExpression havingExpression;

    private final List<OrderExpression> orderByExpressions = new ArrayList<>();

    private Integer limitCount;

    private Integer limitOffset;


    @Override
    public Map<String, Object> getSelectionMap() {
        return selectionMap;
    }

    @Override
    public Optional<DataSource> getDataSource() {
        return Optional.ofNullable(dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Map<String, String> getTableAliases() {
        return tableAliases;
    }

    @Override
    public Map<String, List<String>> getAvailableColumns() {
        return availableColumns;
    }

    @Override
    public Optional<BooleanExpression> getWhereExpression() {
        return Optional.ofNullable(whereExpression);
    }

    public void setWhereExpression(BooleanExpression whereExpression) {
        this.whereExpression = whereExpression;
    }

    @Override
    public List<Object> getGroupByExpressions() {
        return groupByExpressions;
    }

    @Override
    public Optional<BooleanExpression> getHavingExpression() {
        return Optional.ofNullable(havingExpression);
    }

    public void setHavingExpression(BooleanExpression havingExpression) {
        this.havingExpression = havingExpression;
    }

    @Override
    public List<OrderExpression> getOrderByExpressions() {
        return orderByExpressions;
    }

    @Override
    public Optional<Integer> getLimitCount() {
        return Optional.ofNullable(limitCount);
    }

    public void setLimitCount(Integer limitCount) {
        this.limitCount = limitCount;
    }

    @Override
    public Optional<Integer> getLimitOffset() {
        return Optional.ofNullable(limitOffset);
    }

    public void setLimitOffset(Integer limitOffset) {
        this.limitOffset = limitOffset;
    }

    
}
