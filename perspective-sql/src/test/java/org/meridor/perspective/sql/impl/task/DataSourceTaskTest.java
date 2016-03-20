package org.meridor.perspective.sql.impl.task;

import org.junit.Before;
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
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.function.Function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DataSourceTaskTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private MockDataFetcher dataFetcher;
    
    private static final String FIRST_TABLE = "instances";
    private static final String SECOND_TABLE = "projects";
    private static final String FIRST_ID = "id";
    private static final String FIRST_PROJECT_ID = "project_id";
    private static final String SECOND_PROJECT_ID = "project_id";
    private static final String SECOND_NAME = "name";
    
    private static final String FIRST_ALIAS = "first";
    private static final String SECOND_ALIAS = "second";
    
    @Before
    public void before() {
        dataFetcher.setTableData(
                TableName.INSTANCES,
                Arrays.asList(FIRST_ID, FIRST_PROJECT_ID),
                Arrays.asList(
                    Arrays.asList("instance-1", "project-1"),
                    Arrays.asList("instance-2", "project-2")
                )
        );
        dataFetcher.setTableData(
                TableName.PROJECTS,
                Arrays.asList(SECOND_PROJECT_ID, SECOND_NAME),
                Arrays.asList(
                    Arrays.asList("project-1", "Project One"),
                    Arrays.asList("project-2", "Project Two")
                )
        );
    }
    
    @Test
    public void testSimpleFetchFromTable() throws Exception {
        DataSource dataSource = new DataSource(FIRST_ALIAS);
        DataSourceTask dataSourceTask = applicationContext.getBean(
                DataSourceTask.class,
                dataSource,
                Collections.singletonMap(FIRST_ALIAS, FIRST_TABLE)
        );
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        
        assertThat(executionResult.getCount(), equalTo(2));
        DataContainer dataContainer = executionResult.getData();
        assertThat(dataContainer.getColumnsMap().keySet(), contains(FIRST_ALIAS));
        assertThat(dataContainer.getColumnNames(), contains(FIRST_ID, FIRST_PROJECT_ID));
        List<DataRow> rows = dataContainer.getRows();
        assertThat(rows.size(), equalTo(2));
        assertThat(rows.get(0).getValues(), equalTo(Arrays.asList("instance-1", "project-1")));
        assertThat(rows.get(1).getValues(), equalTo(Arrays.asList("instance-2", "project-2")));
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
        assertThat(rows.get(0).getValues(), contains("instance-1", "project-1", "project-1", "Project One"));
        assertThat(rows.get(1).getValues(), contains("instance-2", "project-2", "project-1", "Project One"));
        assertThat(rows.get(2).getValues(), contains("instance-1", "project-1", "project-2", "Project Two"));
        assertThat(rows.get(3).getValues(), contains("instance-2", "project-2", "project-2", "Project Two"));
    }

    private void doCommonAssertions(ExecutionResult executionResult, int size) {
        assertThat(executionResult.getCount(), equalTo(size));
        DataContainer dataContainer = executionResult.getData();
        assertThat(dataContainer.getColumnNames(), hasSize(4));
        assertThat(dataContainer.getColumnsMap().keySet(), contains(FIRST_ALIAS, SECOND_ALIAS));
        assertThat(dataContainer.getColumnsMap().get(FIRST_ALIAS), hasSize(2));
        assertThat(dataContainer.getColumnsMap().get(SECOND_ALIAS), hasSize(2));
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
        doCommonAssertions(executionResult, 2);
        assertThat(rows.get(0).getValues(), contains("instance-1", "project-1", "project-1", "Project One"));
        assertThat(rows.get(1).getValues(), contains("instance-2", "project-2", "project-2", "Project Two"));
    }
    
    private static SimpleBooleanExpression prepareJoinCondition() {
        return new SimpleBooleanExpression(
                new ColumnExpression(FIRST_PROJECT_ID, FIRST_ALIAS),
                BooleanRelation.EQUAL,
                new ColumnExpression(SECOND_PROJECT_ID, SECOND_ALIAS)
        );
    } 
    
    @Test
    public void testInnerJoinByColumns() throws Exception {
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.INNER);
            ds.getJoinColumns().addAll(Collections.singletonList(FIRST_PROJECT_ID));
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 2);
        assertThat(rows.get(0).getValues(), contains("instance-1", "project-1", "project-1", "Project One"));
        assertThat(rows.get(1).getValues(), contains("instance-2", "project-2", "project-2", "Project Two"));
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
        assertThat(rows.get(0).getValues(), contains("instance-1", "project-1", "project-1", "Project One"));
        assertThat(rows.get(1).getValues(), contains("instance-2", "project-2", "project-2", "Project Two"));
    }

    @Test
    public void testLeftJoinByCondition() throws Exception {
        addLeftJoinRow();
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.setJoinCondition(prepareJoinCondition());
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        assertLeftJoinResults(executionResult);
    }
    
    private void addLeftJoinRow() {
        dataFetcher.addDataRow(
                TableName.INSTANCES,
                //There's no project with ID project-3
                Arrays.asList("instance-3", "project-3")
        );
    }
    
    private void assertLeftJoinResults(ExecutionResult executionResult) {
        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 3);
        assertThat(rows.get(0).getValues(), contains("instance-1", "project-1", "project-1", "Project One"));
        assertThat(rows.get(1).getValues(), contains("instance-2", "project-2", "project-2", "Project Two"));
        assertThat(rows.get(2).getValues(), contains("instance-3", "project-3", null, null));
    }
    
    @Test
    public void testLeftJoinByColumns() throws Exception {
        addLeftJoinRow();
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.getJoinColumns().addAll(Collections.singletonList(FIRST_PROJECT_ID));
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        assertLeftJoinResults(executionResult);
    }

    @Test
    public void testRightJoinByCondition() throws Exception {
        dataFetcher.addDataRow(
                TableName.PROJECTS,
                //There's no instance with project_id = project-3
                Arrays.asList("project-3", "Project Three")
        );
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.RIGHT);
            ds.setJoinCondition(prepareJoinCondition());
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());

        List<DataRow> rows = executionResult.getData().getRows();
        doCommonAssertions(executionResult, 3);
        assertThat(rows.get(0).getValues(), contains("instance-1", "project-1", "project-1", "Project One"));
        assertThat(rows.get(1).getValues(), contains("instance-2", "project-2", "project-2", "Project Two"));
        assertThat(rows.get(2).getValues(), contains(null, null, "project-3", "Project Three"));
    }

    @Test
    public void testNaturalLeftJoin() throws Exception {
        addLeftJoinRow();
        DataSourceTask dataSourceTask = prepareJoinTask(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.setNaturalJoin(true);
            return ds;
        });
        ExecutionResult executionResult = dataSourceTask.execute(new ExecutionResult());
        assertLeftJoinResults(executionResult);
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
                        put(FIRST_ALIAS, FIRST_TABLE);
                        put(SECOND_ALIAS, SECOND_TABLE);
                    }
                }
        );
    }
    
}