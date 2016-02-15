package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.SimpleBooleanExpression;
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
import java.util.function.Function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DataSourceTaskTest {

    @Autowired
    private ApplicationContext applicationContext;
    private static final String TABLE_NAME = "mock";
    private static final String FIRST_COLUMN = "str";
    private static final String SECOND_COLUMN = "num";
    private static final String THIRD_COLUMN = "numWithDefaultValue";
    private static final String FOURTH_COLUMN = "missingDefaultValue";
    
    private static final String FIRST_ALIAS = "first";
    private static final String SECOND_ALIAS = "second";
    
    @Test
    public void testSimpleFetchFromTable() throws Exception {
        DataSource dataSource = new DataSource(FIRST_ALIAS);
        DataSourceTask dataSourceTask = applicationContext.getBean(
                DataSourceTask.class,
                dataSource,
                Collections.singletonMap(FIRST_ALIAS, TABLE_NAME)
        );
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        
        assertThat(executionResult.getCount(), equalTo(2)); //Mock storage always returns 2 rows
        DataContainer dataContainer = executionResult.getData();
        assertThat(dataContainer.getColumnsMap().keySet(), contains(FIRST_ALIAS));
        assertThat(dataContainer.getColumnNames(), contains(FIRST_COLUMN, SECOND_COLUMN, THIRD_COLUMN, FOURTH_COLUMN));
        List<DataRow> rows = dataContainer.getRows();
        assertThat(rows.size(), equalTo(2));
        assertThat(rows.get(0).getValues(), equalTo(Arrays.asList(1, 2, 3, 4)));
        assertThat(rows.get(1).getValues(), equalTo(Arrays.asList(2, 4, 6, 8)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingTable() throws Exception {
        DataSource dataSource = new DataSource("missing");
        DataSourceTask dataSourceTask = applicationContext.getBean(
                DataSourceTask.class,
                dataSource,
                Collections.emptyMap()
        );
        dataSourceTask.execute(new ExecutionResult());
    }

    @Test
    public void testCrossJoin() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.INNER);
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        
        doCommonAssertions(executionResult, 4);
        List<DataRow> rows = executionResult.getData().getRows();
        assertThat(rows.get(0).getValues(), contains(1, 2, 3, 4, 1, 2, 3, 4));
        assertThat(rows.get(1).getValues(), contains(2, 4, 6, 8, 1, 2, 3, 4));
        assertThat(rows.get(2).getValues(), contains(1, 2, 3, 4, 2, 4, 6, 8));
        assertThat(rows.get(3).getValues(), contains(2, 4, 6, 8, 2, 4, 6, 8));
    }

    private void doCommonAssertions(ExecutionResult executionResult, int size) {
        assertThat(executionResult.getCount(), equalTo(size));
        DataContainer dataContainer = executionResult.getData();
        assertThat(dataContainer.getColumnNames(), hasSize(8));
        assertThat(dataContainer.getColumnsMap().keySet(), contains(FIRST_ALIAS, SECOND_ALIAS));
        assertThat(dataContainer.getColumnsMap().get(FIRST_ALIAS), hasSize(4));
        assertThat(dataContainer.getColumnsMap().get(SECOND_ALIAS), hasSize(4));
        List<DataRow> rows = dataContainer.getRows();
        assertThat(rows, hasSize(size));
    }
    
    @Test
    public void testInnerJoinByCondition() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.INNER);
            ds.setJoinCondition(prepareJoinCondition());
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        
        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 1);
        assertThat(rows.get(0).getValues(), contains(2, 4, 6, 8, 1, 2, 3, 4));
    }
    
    private static SimpleBooleanExpression prepareJoinCondition() {
        return new SimpleBooleanExpression(
                new ColumnExpression(SECOND_COLUMN, FIRST_ALIAS),
                BooleanRelation.EQUAL,
                new ColumnExpression(FOURTH_COLUMN, SECOND_ALIAS)
        );
    } 
    
    @Test
    public void testInnerJoinByColumns() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.INNER);
            ds.getJoinColumns().addAll(Collections.singletonList(SECOND_COLUMN));
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 2);
        assertThat(rows.get(0).getValues(), contains(1, 2, 3, 4, 1, 2, 3, 4));
        assertThat(rows.get(1).getValues(), contains(2, 4, 6, 8, 2, 4, 6, 8));
    }

    @Test
    public void testNaturalInnerJoin() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.INNER);
            ds.setNaturalJoin(true);
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 2);
        assertThat(rows.get(0).getValues(), contains(1, 2, 3, 4, 1, 2, 3, 4));
        assertThat(rows.get(1).getValues(), contains(2, 4, 6, 8, 2, 4, 6, 8));
    }

    @Test
    public void testLeftJoinByCondition() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.setJoinCondition(prepareJoinCondition());
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 3);
        assertThat(rows.get(0).getValues(), contains(2, 4, 6, 8, 1, 2, 3, 4));
        assertThat(rows.get(1).getValues(), contains(1, 2, 3, 4, null, null, null, null));
        assertThat(rows.get(2).getValues(), contains(2, 4, 6, 8, null, null, null, null));

    }
    
    @Test
    public void testLeftJoinByColumns() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.getJoinColumns().addAll(Collections.singletonList(SECOND_COLUMN));
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 4);
        assertThat(rows.get(0).getValues(), contains(1, 2, 3, 4, 1, 2, 3, 4));
        assertThat(rows.get(1).getValues(), contains(2, 4, 6, 8, 2, 4, 6, 8));
        assertThat(rows.get(2).getValues(), contains(1, 2, 3, 4, null, null, null, null));
        assertThat(rows.get(3).getValues(), contains(2, 4, 6, 8, null, null, null, null));

    }

    @Test
    public void testRightJoinByCondition() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.RIGHT);
            ds.setJoinCondition(prepareJoinCondition());
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 3);
        assertThat(rows.get(0).getValues(), contains(2, 4, 6, 8, 1, 2, 3, 4));
        assertThat(rows.get(1).getValues(), contains(null, null, null, null, 1, 2, 3, 4));
        assertThat(rows.get(2).getValues(), contains(null, null, null, null, 2, 4, 6, 8));
    }

    @Test
    public void testNaturalLeftJoin() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.setNaturalJoin(true);
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 4);
        assertThat(rows.get(0).getValues(), contains(1, 2, 3, 4, 1, 2, 3, 4));
        assertThat(rows.get(1).getValues(), contains(2, 4, 6, 8, 2, 4, 6, 8));
        assertThat(rows.get(2).getValues(), contains(1, 2, 3, 4, null, null, null, null));
        assertThat(rows.get(3).getValues(), contains(2, 4, 6, 8, null, null, null, null));

    }

    private DataSourceTask prepareJoinTask(Function<DataSource, DataSource> secondDataSourceProcessor) {
        DataSource first = new DataSource(FIRST_ALIAS);
        DataSource second = secondDataSourceProcessor.apply(new DataSource(SECOND_ALIAS));
        first.setNextDatasource(second);
        return applicationContext.getBean(
                DataSourceTask.class,
                first,
                new HashMap<String, String>(){
                    {
                        put(FIRST_ALIAS, TABLE_NAME);
                        put(SECOND_ALIAS, TABLE_NAME);
                    }
                }
        );
    }
    
}