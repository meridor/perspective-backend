package org.meridor.perspective.sql.impl.index.impl;

import org.junit.Test;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;
import org.meridor.perspective.sql.impl.index.Keys;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class HashTableIndexTest {

    @Test(timeout = 10000)
    public void testIndexSpeed() {
        IndexSignature indexSignature = new IndexSignature("test", Collections.singleton("id"));
        Index index = new HashTableIndex(indexSignature);
        Key key = Keys.key("key");
        IntStream.rangeClosed(0, 99999)
                .forEach(value -> index.put(key, String.valueOf(value)));
        assertThat(index.getKeys(), contains(key));
        assertThat(index.get(key), hasSize(100000));
    }
    
}