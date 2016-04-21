package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Row;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.DASH;
import static org.meridor.perspective.sql.DataUtils.get;

public class ValueFormatter {
    private final Data data;
    private final Row row;

    public ValueFormatter(Data data, Row row) {
        this.data = data;
        this.row = row;
    }
    
    public String getString(String name) {
        Object value = get(data, row, name);
        return value != null ? String.valueOf(value) : DASH;
    }
}
