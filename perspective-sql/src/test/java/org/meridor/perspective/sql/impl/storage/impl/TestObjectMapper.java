package org.meridor.perspective.sql.impl.storage.impl;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Component
public class TestObjectMapper extends BaseObjectMapper<TestObject> {
    
    public static final String COLUMN_NAME = "str";
    
    @Override
    protected Map<String, Function<TestObject, Object>> getColumnMapping() {
        return Collections.singletonMap(COLUMN_NAME, TestObject::getStr);
    }

    @Override
    public Class<TestObject> getInputClass() {
        return TestObject.class;
    }

    @Override
    public String getId(TestObject val) {
        return val.getStr();
    }
}
