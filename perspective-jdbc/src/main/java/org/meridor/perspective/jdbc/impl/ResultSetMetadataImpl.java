package org.meridor.perspective.jdbc.impl;

import org.meridor.perspective.jdbc.BaseEntity;
import org.meridor.perspective.jdbc.PerspectiveResultSet;
import org.meridor.perspective.sql.Row;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import static org.meridor.perspective.jdbc.impl.DataUtils.*;

public class ResultSetMetadataImpl extends BaseEntity implements ResultSetMetaData {
    
    private final PerspectiveResultSet resultSet;

    public ResultSetMetadataImpl(PerspectiveResultSet perspectiveResultSet) {
        this.resultSet = perspectiveResultSet;
    }

    private PerspectiveResultSet getResultSet() {
        return resultSet;
    }

    private boolean isDataEmpty() {
        return getDataSize(getResultSet().getData()) == 0;
    }
    
    private Row getFirstRow() {
        return getRow(getResultSet().getData(), 0);
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return isDataEmpty() ?
                0 :
                getColumnsNames(getResultSet().getData()).size();
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return ResultSetMetaData.columnNullableUnknown;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return 0;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return getColumnName(column);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        List<String> columnsNames = DataUtils.getColumnsNames(resultSet.getData());
        if (column < 0 || column >= columnsNames.size()) {
            throw new SQLException(String.format("Column with index %d does not exist", column));
        }
        return columnsNames.get(column);
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return 0;
    }

    @Override
    public int getScale(int column) throws SQLException {
        return 0;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return "";
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return "perspective";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        Class<?> columnClass = getColumnClass(column);
        if (Byte.class.equals(columnClass)) {
            return Types.TINYINT;
        } else if (Short.class.equals(columnClass)) {
            return Types.SMALLINT;
        } else if (Integer.class.equals(columnClass)) {
            return Types.INTEGER;
        } else if (Long.class.equals(columnClass)) {
            return Types.BIGINT;
        } else if (Double.class.equals(columnClass)) {
            return Types.DOUBLE;
        } else if (Float.class.equals(columnClass)) {
            return Types.FLOAT;
        } else if (String.class.equals(columnClass)) {
            return Types.VARCHAR;
        } else if (Date.class.equals(columnClass)) {
            return Types.DATE;
        } else {
            return Types.OTHER;
        }
    }

    private Class<?> getColumnClass(int column) throws SQLException {
        if (column > getColumnCount() || column < 1) {
            throw new SQLException(String.format("Invalid column index: %d", column));
        }
        return isDataEmpty() ? 
                get(getResultSet().getData(), getFirstRow(), column).getClass() :
                Object.class;
    }
    
    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return getColumnClassName(column);
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return getColumnClass(column).getCanonicalName();
    }
}
