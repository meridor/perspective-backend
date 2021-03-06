package org.meridor.perspective.sql.impl.task.strategy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.HashTableIndex;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.index.Keys.key;
import static org.meridor.perspective.sql.impl.task.strategy.StrategyTestUtils.*;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class IndexFetchStrategyTest {

    private static final String ANY_ID = "id";
    private static final String ANY_OTHER_ID = "other-id";

    @Autowired
    private IndexStorage indexStorage;
    
    @Autowired
    private ApplicationContext applicationContext;

    private static final Set<String> COLUMNS = new HashSet<String>(){
        {
            add("one");
            add("two");
        }
    };
    private static final IndexSignature INDEX_SIGNATURE = new IndexSignature(TABLE_NAME, COLUMNS);
    private static final List<Object[]> COLUMN_VALUES = new ArrayList<Object[]>(){
        {
            add(new Object[]{"11", "12"});
            add(new Object[]{"21", "22"});
        }
    };

    @Before
    public void init() {
        Index index = new HashTableIndex(INDEX_SIGNATURE);
        index.put(key(0, COLUMN_VALUES.get(0)), ANY_ID);
        index.put(key(0, COLUMN_VALUES.get(0)), ANY_OTHER_ID); //Two different ids for this key!
        index.put(key(0, COLUMN_VALUES.get(1)), ANY_ID);
        indexStorage.update(INDEX_SIGNATURE, any -> index);
    }
    
    @Test
    public void testProcess() {
        
        DataSource dataSource = new DataSource(TABLE_ALIAS);
        dataSource.getColumns().addAll(COLUMNS);

        DataSourceStrategy strategy = getStrategy();
        DataContainer result = strategy.process(dataSource, TABLE_ALIASES);
        
        assertThat(result.getColumnsMap().keySet(), contains(TABLE_ALIAS));
        String[] columnsAsArray = COLUMNS.toArray(new String[COLUMNS.size()]);
        assertThat(result.getColumnsMap().get(TABLE_ALIAS), contains(columnsAsArray));


        List<DataRow> rows = result.getRows();
        assertThat(rows, hasSize(3));

        Set<Object[]> rowsAsValues = rows.stream().map(dr -> dr.getValues().toArray()).collect(Collectors.toSet());
        assertThat(
                rowsAsValues,
                containsInAnyOrder( //Should return a key for each id
                        COLUMN_VALUES.get(0),
                        COLUMN_VALUES.get(0),
                        COLUMN_VALUES.get(1))
        ); 
    }
    
    private DataSourceStrategy getStrategy() {
        return applicationContext.getBean(IndexFetchStrategy.class);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNoTableAlias() {
        DataSource dataSource = new DataSource(new DataSource(TABLE_ALIAS));
        assertThat(dataSource.getTableAlias().isPresent(), is(false));
        DataSourceStrategy strategy = getStrategy();
        strategy.process(dataSource, TABLE_ALIASES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJoinIsPresent() {
        DataSource dataSource = new DataSource(TABLE_ALIAS);
        dataSource.setRightDataSource(new DataSource("anything"));
        DataSourceStrategy strategy = getStrategy();
        strategy.process(dataSource, TABLE_ALIASES);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyColumnNames() {
        DataSource dataSource = new DataSource(TABLE_ALIAS);
        assertThat(dataSource.getColumns(), is(empty()));
        DataSourceStrategy strategy = getStrategy();
        strategy.process(dataSource, TABLE_ALIASES);
    }
    
}