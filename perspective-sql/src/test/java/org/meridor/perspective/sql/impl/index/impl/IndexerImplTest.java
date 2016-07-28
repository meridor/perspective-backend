package org.meridor.perspective.sql.impl.index.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Indexer;
import org.meridor.perspective.sql.impl.index.Key;
import org.meridor.perspective.sql.impl.index.Keys;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.storage.impl.TestObject;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class IndexerImplTest {
    
    private static final String TABLE_NAME = "mock";
    private static final IndexSignature INDEX_SIGNATURE = new IndexSignature(
            Collections.singletonMap("mock", Collections.singleton("str"))
    );
    private static final String ID = "id";
    private static final TestObject BEAN = new TestObject(ID);
    
    @Autowired
    private IndexStorage indexStorage;
    
    @Autowired
    private Indexer indexer;
    
    @Test
    public void testAddThenDelete() {
        assertThat(getIndex().isPresent(), is(true));
        indexer.add(TABLE_NAME, BEAN);
        Index indexWithAddedBean = getIndex().get();
        Key key = Keys.key(indexWithAddedBean.getKeyLength(), BEAN.getStr());
        Set<String> ids = indexWithAddedBean.get(key);
        assertThat(ids, contains(ID));
        indexer.delete(TABLE_NAME, BEAN);

        Index indexWithDeletedBean = getIndex().get();
        assertThat(indexWithDeletedBean.get(key), is(empty()));
    }

    private Optional<Index> getIndex() {
        return indexStorage.get(INDEX_SIGNATURE);
    }
    
}