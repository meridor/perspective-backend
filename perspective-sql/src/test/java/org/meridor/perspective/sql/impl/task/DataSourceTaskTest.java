package org.meridor.perspective.sql.impl.task;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
//TODO: implement it!
public class DataSourceTaskTest {

    @Autowired
    private ApplicationContext applicationContext;
    private static final String TABLE_NAME = "mock";
    private static final String FIRST_COLUMN = "id";
    private static final String SECOND_COLUMN = "name";
    
    private static final String FIRST_ALIAS = "first";
    private static final String SECOND_ALIAS = "second";
    
    @Test
    public void testSimpleSelect() throws Exception {
        DataSource dataSource = new DataSource(FIRST_ALIAS);
        DataSourceTask dataSourceTask = applicationContext.getBean(
                DataSourceTask.class,
                dataSource,
                Collections.singletonMap(FIRST_ALIAS, TABLE_NAME)
        );
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(2)); //Mock storage always returns 2 rows
        DataContainer dataContainer = executionResult.getData();
        assertThat(dataContainer.getColumnNames(), contains(FIRST_COLUMN, SECOND_COLUMN));
        List<DataRow> rows = dataContainer.getRows();
        assertThat(rows.size(), equalTo(2));
        assertThat(rows.get(0).getValues(), equalTo(Arrays.asList(1, 1)));
        assertThat(rows.get(1).getValues(), equalTo(Arrays.asList(2, 2)));
    }
    
    @Test
    public void testSelectAll() throws Exception {
        //TODO: implement it! Tests SELECT * FROM table
//        SelectTask selectTask = applicationContext.getBean(
//                SelectTask.class,
//                TABLE_NAME,
//                Collections.emptyList()
//        );
//        ExecutionResult executionResult = selectTask.execute(new ExecutionResult());
//        assertThat(executionResult.getCount(), equalTo(2));
//        List<DataRow> data = executionResult.getData().getRows();
//        assertThat(data.get(0).getValues(), hasSize(4)); //All columns were selected
    }
    
    @Test
    public void testMissingTable() throws Exception {
        DataSource dataSource = new DataSource("missing");
        DataSourceTask dataSourceTask = applicationContext.getBean(
                DataSourceTask.class,
                dataSource,
                Collections.emptyMap()
        );
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(0));
        assertThat(executionResult.getData().getRows(), is(empty()));
    }
    
    @Test
    public void testCrossJoin() throws Exception {
        DataSource first = new DataSource(FIRST_ALIAS);
        DataSource second = new DataSource(SECOND_ALIAS);
        second.setJoinType(JoinType.INNER);
        first.setNextDatasource(first);
        DataSourceTask dataSourceTask = applicationContext.getBean(
                DataSourceTask.class,
                first,
                new HashMap<String, String>(){
                    {
                        put(FIRST_ALIAS, TABLE_NAME);
                        put(SECOND_ALIAS, TABLE_NAME);
                    }
                }
        );
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(4));
        DataContainer dataContainer = executionResult.getData();
        assertThat(dataContainer.getColumnsMap().keySet(), contains(FIRST_ALIAS, SECOND_ALIAS));
        //TODO: to be continued!
    }
    
    @Test
    public void testInnerJoinByCondition() throws Exception {
        //TODO: implement it!
    }
    
    @Test
    public void testInnerJoinByColumns() throws Exception {
        //TODO: implement it!
    }

    @Test
    public void testNaturalInnerJoin() throws Exception {
        //TODO: implement it!
    }
    @Test
    public void testLeftJoin() throws Exception {
        //TODO: implement it!
    }

    @Test
    public void testNaturalLeftJoin() throws Exception {
        //TODO: implement it!
    }
    
    @Test
    public void testRightJoin() throws Exception {
        //TODO: implement it!
    }
    
}