package org.meridor.perspective.sql.impl.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataSource {

    private Optional<String> tableAlias = Optional.empty();
    private Optional<DataSource> dataSource = Optional.empty();
    private boolean isNaturalJoin;
    private Optional<JoinType> joinType = Optional.empty();
    private Optional<Object> condition = Optional.empty();
    private final List<String> joinColumns = new ArrayList<>();
    private Optional<DataSource> nextDatasource = Optional.empty();

    public DataSource(String tableAlias) {
        this.tableAlias = Optional.ofNullable(tableAlias);
    }
    
    public DataSource(DataSource dataSource) {
        this.dataSource = Optional.ofNullable(dataSource);
    }

    public Optional<String> getTableAlias() {
        return tableAlias;
    }

    public Optional<DataSource> getDataSource() {
        return dataSource;
    }

    public Optional<JoinType> getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = Optional.ofNullable(joinType);
    }

    public Optional<Object> getCondition() {
        return condition;
    }

    public void setCondition(Object condition) {
        this.condition = Optional.ofNullable(condition);
    }

    public Optional<DataSource> getNextDataSource() {
        return nextDatasource;
    }

    public void setNextDatasource(DataSource nextDataSource) {
        this.nextDatasource = Optional.ofNullable(nextDataSource);
    }

    public List<String> getJoinColumns() {
        return joinColumns;
    }

    public boolean isNaturalJoin() {
        return isNaturalJoin;
    }

    public void setNaturalJoin(boolean naturalJoin) {
        isNaturalJoin = naturalJoin;
    }
}
