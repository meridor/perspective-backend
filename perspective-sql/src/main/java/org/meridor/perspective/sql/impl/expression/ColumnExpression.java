package org.meridor.perspective.sql.impl.expression;

import org.meridor.perspective.sql.impl.table.Column;

public class ColumnExpression {
    
    private final String columnName;

    private final String tableAlias;

    public ColumnExpression() {
        this(Column.ANY, Column.ANY);
    }

    public ColumnExpression(String columnName, String tableAlias) {
        this.columnName = columnName;
        this.tableAlias = tableAlias;
    }
    
    public ColumnExpression(String columnName) {
        this(columnName, Column.ANY);
    }
    
    public String getColumnName() {
        return columnName;
    }

    public String getTableAlias() {
        return tableAlias;
    }
    
    public boolean useAnyTable() {
        return Column.ANY.equals(getTableAlias());
    }

}
