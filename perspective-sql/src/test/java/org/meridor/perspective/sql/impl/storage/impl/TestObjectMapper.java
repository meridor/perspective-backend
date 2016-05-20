package org.meridor.perspective.sql.impl.storage.impl;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Component
public class TestObjectMapper extends BaseObjectMapper<Long> {
    
    public static final String COLUMN_NAME = "value";
    
    @Override
    protected Map<String, Function<Long, Object>> getColumnMapping() {
        return Collections.singletonMap(COLUMN_NAME, l -> l);
    }

    @Override
    public Class<Long> getInputClass() {
        return Long.class;
    }

    @Override
    public String getId(Long val) {
        return val.toString();
    }
}
