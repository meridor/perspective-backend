package org.meridor.perspective.sql.impl.expression;

public class ColumnExpression {
    
    private final String columnName;

    private final String tableName;

    public ColumnExpression(String columnName, String tableName) {
        this.columnName = columnName;
        this.tableName = tableName;
    }
    
    public String getColumnName() {
        return columnName;
    }

    public String getTableName() {
        return tableName;
    }
}
