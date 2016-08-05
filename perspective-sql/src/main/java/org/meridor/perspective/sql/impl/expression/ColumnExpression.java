package org.meridor.perspective.sql.impl.expression;

import static org.meridor.perspective.sql.impl.table.Column.*;

public class ColumnExpression {
    
    private final String columnName;

    private final String tableAlias;

    public ColumnExpression() {
        this(ANY_COLUMN, ANY_TABLE);
    }

    public ColumnExpression(String columnName, String tableAlias) {
        this.columnName = columnName;
        this.tableAlias = tableAlias;
    }
    
    public ColumnExpression(String columnName) {
        this(columnName, ANY_TABLE);
    }
    
    public String getColumnName() {
        return columnName;
    }

    public String getTableAlias() {
        return tableAlias;
    }
    
    public boolean useAnyTable() {
        return ANY_TABLE.equals(getTableAlias());
    }

    public boolean useAnyColumn() {
        return ANY_COLUMN.equals(getColumnName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnExpression that = (ColumnExpression) o;

        return columnName.equals(that.columnName) && tableAlias.equals(that.tableAlias);

    }

    @Override
    public String toString() {
        if (useAnyTable()) {
            return useAnyColumn() ? "*" : getColumnName();
        }
        return String.format(
                "%s.%s",
                getTableAlias(),
                useAnyColumn() ? "*" : getColumnName()
        );
    }
}
