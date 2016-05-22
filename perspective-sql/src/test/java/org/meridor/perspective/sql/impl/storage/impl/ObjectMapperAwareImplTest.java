package org.meridor.perspective.sql.impl.storage.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.impl.storage.ObjectMapper;
import org.meridor.perspective.sql.impl.storage.ObjectMapperAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ObjectMapperAwareImplTest {
    
    @Autowired
    private ObjectMapperAware objectMapperAware;
    
    @Test
    public void testGetExistingObjectMapper() {
        ObjectMapper<TestObject> testObjectMapper = objectMapperAware.get(TestObject.class);
        final String VALUE = "test";
        final TestObject OBJECT = new TestObject(VALUE);
        Map<String, Object> mappedValues = testObjectMapper.map(OBJECT);
        assertThat(mappedValues.keySet(), contains(TestObjectMapper.COLUMN_NAME));
        assertThat(mappedValues.get(TestObjectMapper.COLUMN_NAME), equalTo(VALUE));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetMissingObjectMapper() {
        objectMapperAware.get(Void.class);
    }

}