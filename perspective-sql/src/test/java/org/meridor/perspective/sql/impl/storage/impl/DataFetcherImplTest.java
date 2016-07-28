package org.meridor.perspective.sql.impl.storage.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.expression.MockTable;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/data-fetcher-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DataFetcherImplTest {

    @Autowired
    private DataFetcher dataFetcher;

    @Test
    public void testFetchExistingTable() {
        final String TABLE_ALIAS = "alias";
        final String COLUMN_NAME = "column";
        DataContainer data = dataFetcher.fetch(
                "existing",
                TABLE_ALIAS,
                Collections.singletonList(new Column(COLUMN_NAME, String.class, null, new MockTable()))
        );
        Map<String, List<String>> columnsMap = data.getColumnsMap();
        assertThat(columnsMap.keySet(), hasSize(1));
        assertThat(columnsMap.keySet(), contains(TABLE_ALIAS));
        assertThat(columnsMap.get(TABLE_ALIAS), hasSize(1));
        assertThat(columnsMap.get(TABLE_ALIAS), contains(COLUMN_NAME));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchMissingTable() {
        dataFetcher.fetch(
                "missing",
                "anything",
                Collections.emptyList()
        );
    }

}