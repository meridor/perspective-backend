package org.meridor.perspective.sql.impl.parser;

import org.meridor.perspective.sql.impl.expression.BooleanExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataSource {

    private String tableAlias;
    private DataSource leftDataSource;
    private boolean isNaturalJoin;
    private JoinType joinType;
    private BooleanExpression condition;
    private final List<String> columns = new ArrayList<>();
    private DataSource rightDatasource;
    private DataSourceType type = DataSourceType.TABLE_SCAN; 

    public DataSource(String tableAlias) {
        this.tableAlias = tableAlias;
    }
    
    public DataSource(DataSource leftDataSource) {
        this.leftDataSource = leftDataSource;
        this.type = DataSourceType.PARENT;
    }
    
    public DataSource() {
        this.type = DataSourceType.PARENT;
    }

    public Optional<String> getTableAlias() {
        return Optional.ofNullable(tableAlias);
    }
    
    public void setLeftDataSource(DataSource leftDataSource) {
        this.leftDataSource = leftDataSource;
    }

    public Optional<DataSource> getLeftDataSource() {
        return Optional.ofNullable(leftDataSource);
    }

    public Optional<JoinType> getJoinType() {
        return Optional.ofNullable(joinType);
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public Optional<BooleanExpression> getCondition() {
        return Optional.ofNullable(condition);
    }

    public void setCondition(BooleanExpression condition) {
        this.condition = condition;
    }

    public Optional<DataSource> getRightDataSource() {
        return Optional.ofNullable(rightDatasource);
    }

    public void setRightDataSource(DataSource rightDataSource) {
        this.rightDatasource = rightDataSource;
    }

    public List<String> getColumns() {
        return columns;
    }

    public boolean isNaturalJoin() {
        return isNaturalJoin;
    }

    public void setNaturalJoin(boolean naturalJoin) {
        isNaturalJoin = naturalJoin;
    }

    public DataSource copy() {
        DataSource dataSource = getTableAlias().isPresent() ? 
                new DataSource(getTableAlias().get()) :
                new DataSource(getLeftDataSource().get().copy());
        dataSource.setCondition(condition);
        dataSource.setNaturalJoin(isNaturalJoin);
        dataSource.setJoinType(joinType);
        dataSource.getColumns().addAll(columns);
        if (getRightDataSource().isPresent()) {
            dataSource.setRightDataSource(getRightDataSource().get().copy());
        }
        return dataSource;
    }

    public DataSourceType getType() {
        return type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) return true;
        if (another == null || getClass() != another.getClass()) return false;

        DataSource that = (DataSource) another;

        if (isNaturalJoin != that.isNaturalJoin) return false;
        if (tableAlias != null ? !tableAlias.equals(that.tableAlias) : that.tableAlias != null)
            return false;
        if (leftDataSource != null ? !leftDataSource.equals(that.leftDataSource) : that.leftDataSource != null)
            return false;
        if (joinType != that.joinType) return false;
        if (condition != null ? !condition.equals(that.condition) : that.condition != null)
            return false;
        if (!columns.equals(that.columns)) return false;
        if (rightDatasource != null ? !rightDatasource.equals(that.rightDatasource) : that.rightDatasource != null)
            return false;
        return type == that.type;

    }

    public enum DataSourceType {
        PARENT,
        INDEX_FETCH,
        INDEX_SCAN,
        TABLE_SCAN,
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DataSource{\n");
        if (tableAlias != null) {
            sb.append(String.format("\ttableAlias='%s',\n", String.valueOf(tableAlias)));
        }
        if (leftDataSource != null) {
            sb.append(String.format("\tleftDataSource=%s,\n", String.valueOf(leftDataSource).replace("\n", "\n\t")));
        }
        if (isNaturalJoin) {
            sb.append("\tnaturalJoin=true,\n");
        }
        if (joinType != null) {
            sb.append(String.format("\tjoinType=%s,\n", String.valueOf(joinType)));
        }
        if (condition != null) {
            sb.append(String.format("\tcondition=%s,\n", String.valueOf(condition)));
        }
        if (!columns.isEmpty()) {
            sb.append(String.format("\tcolumns=%s,\n", columns));
        }
        if (rightDatasource != null) {
            sb.append(String.format(
                    "\trightDatasource=%s,\n",
                    String.valueOf(rightDatasource).replace("\n", "\n\t")
            ));
        }
        sb.append(String.format("\ttype=%s\n", String.valueOf(type)));
        sb.append("}");
        return sb.toString();
    }
}
