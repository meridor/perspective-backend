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
        ObjectMapper<Long> testObjectMapper = objectMapperAware.get(Long.class);
        final long VALUE = 42L;
        Map<String, Object> mappedLong = testObjectMapper.map(VALUE);
        assertThat(mappedLong.keySet(), contains(TestObjectMapper.COLUMN_NAME));
        assertThat(mappedLong.get(TestObjectMapper.COLUMN_NAME), equalTo(VALUE));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetMissingObjectMapper() {
        objectMapperAware.get(Void.class);
    }

}