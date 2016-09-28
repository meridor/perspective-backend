package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Row;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.DASH;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.joinLines;
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

    public String getString(Map<String, String> names) {
        List<String> notNullValues = names.keySet().stream()
                .map(n -> {
                    Object value = get(data, row, n);
                    String name = names.get(n);
                    return value != null ? String.format("%s: %s", name, value) : null;
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());
        return !notNullValues.isEmpty() ? joinLines(notNullValues) : DASH;
    }
}
