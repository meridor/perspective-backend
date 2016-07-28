package org.meridor.perspective.rest.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Key;
import org.meridor.perspective.sql.impl.index.Keys;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/index-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class IndexStorageImplTest {

    private static final String TABLE_NAME = "test-table";
    private static final String COLUMN_NAME = "test-column";
    private static final int KEY_LENGTH = 0;
    private static final Key KEY = Keys.key(KEY_LENGTH, "key"); 
    private static final String ID = "id"; 
    
    @Autowired
    private IndexStorage indexStorage;
    
    @Test
    public void testIndexDataSaved() {
        IndexSignature indexSignature = new IndexSignature(TABLE_NAME, Collections.singleton(COLUMN_NAME));
        indexStorage.create(indexSignature, KEY_LENGTH);
        assertThat(indexStorage.get(indexSignature).isPresent(), is(true));
        Index indexBeforeModification = indexStorage.get(indexSignature).get();
        assertThat(indexBeforeModification.getKeys(), is(empty()));
        indexStorage.update(indexSignature, index -> {
            index.put(KEY, ID);
            return index;
        });
        Index indexAfterModification = indexStorage.get(indexSignature).get();
        assertThat(indexAfterModification.getKeys(), contains(KEY));
        assertThat(indexAfterModification.get(KEY), contains(ID));
    }
    
}