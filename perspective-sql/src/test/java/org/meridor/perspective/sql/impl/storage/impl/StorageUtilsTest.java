package org.meridor.perspective.sql.impl.storage.impl;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;
import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

public class StorageUtilsTest {

    @Test
    public void testCreateCompositeId() {
        assertThat(createCompositeId("1", "2", "3"), equalTo("1:2:3"));
    }
    
    @Test
    public void testParseCompositeId() {
        assertThat(Arrays.asList(parseCompositeId("1:2:3", 3)), contains("1", "2", "3"));
        assertThat(Arrays.asList(parseCompositeId(null, 0)), is(empty()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidCompositeId() {
        parseCompositeId("1:2:3", 4);
    }
}