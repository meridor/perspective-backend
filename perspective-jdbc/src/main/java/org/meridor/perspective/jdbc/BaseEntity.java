package org.meridor.perspective.jdbc;

import java.sql.SQLException;
import java.sql.Wrapper;

public abstract class BaseEntity implements Wrapper {

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        } catch (Exception e) {
            throw new SQLException(String.format("Failed to unwrap %s", iface.getCanonicalName()));
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(getClass());
    }

}
