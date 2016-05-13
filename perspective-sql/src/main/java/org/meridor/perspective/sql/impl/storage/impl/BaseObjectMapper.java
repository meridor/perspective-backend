package org.meridor.perspective.sql.impl.storage.impl;


import org.meridor.perspective.sql.impl.storage.ObjectMapper;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class BaseObjectMapper<T> implements ObjectMapper<T> {

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    protected abstract Map<String, Function<T, Object>> getColumnMapping();

    @Override
    public Map<String, Object> map(T input) {
        Map<String, Object> ret = new LinkedHashMap<>();
        Map<String, Function<T, Object>> columnMapping = getColumnMapping();
        columnMapping.keySet().forEach(cn -> {
            Object columnValue = columnMapping.get(cn).apply(input);
            ret.put(cn, columnValue);
        });
        return ret;
    }

    @Override
    public Set<String> getAvailableColumnNames() {
        return getColumnMapping().keySet();
    }

}
