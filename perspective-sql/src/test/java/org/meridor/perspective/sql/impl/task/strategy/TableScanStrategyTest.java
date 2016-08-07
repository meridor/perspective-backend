package org.meridor.perspective.sql.impl.task.strategy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.SimpleBooleanExpression;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.meridor.perspective.sql.impl.task.MockDataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.task.strategy.StrategyTestUtils.*;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TableScanStrategyTest {

    private static final String INSTANCES_TABLE = "instances";
    private static final String PROJECTS_TABLE = "projects";
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockDataFetcher dataFetcher;

    @Before
    public void before() {
        dataFetcher.setTableData(INSTANCES, INSTANCES_COLUMNS, INSTANCES_DATA);
        dataFetcher.setTableData(PROJECTS, PROJECTS_COLUMNS, PROJECTS_DATA);
    }

    @Test
    public void testSimpleFetchFromTable() throws Exception {
        DataSource dataSource = new DataSource(INSTANCES_ALIAS);
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, Collections.singletonMap(INSTANCES_ALIAS, INSTANCES_TABLE));
        assertThat(dataContainer.getColumnsMap(), equalTo(INSTANCES_COLUMNS_MAP));
        List<DataRow> rows = dataContainer.getRows();
        assertThat(rows.size(), equalTo(5));
        assertThat(rowsAsValues(rows), equalTo(INSTANCES_DATA));
    }

    private DataSourceStrategy getStrategy() {
        return applicationContext.getBean(TableScanStrategy.class);
    }
    
    private static List<List<Object>> rowsAsValues(List<DataRow> rows) {
        return rows.stream().map(DataRow::getValues).collect(Collectors.toList());
    } 

    @Test(expected = IllegalArgumentException.class)
    public void testMissingTable() throws Exception {
        DataSource dataSource = new DataSource("missing");
        DataSourceStrategy strategy = getStrategy();
        strategy.process(dataSource, Collections.emptyMap());
    }

    @Test
    public void testCrossJoin() throws Exception {
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.INNER);
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);

        doCommonAssertions(dataContainer, 15);
        List<DataRow> rows = dataContainer.getRows();
        List<List<Object>> correctData = Arrays.asList(
                Arrays.asList("1", "first", "2", "1", "first_project"),
                Arrays.asList("2", "second", "1", "1", "first_project"),
                Arrays.asList("3", "third", "2", "1", "first_project"),
                Arrays.asList("4", "third", "3", "1", "first_project"),
                Arrays.asList("5", "fifth", "2", "1", "first_project"),
                Arrays.asList("1", "first", "2", "2", "second_project"),
                Arrays.asList("2", "second", "1", "2", "second_project"),
                Arrays.asList("3", "third", "2", "2", "second_project"),
                Arrays.asList("4", "third", "3", "2", "second_project"),
                Arrays.asList("5", "fifth", "2", "2", "second_project"),
                Arrays.asList("1", "first", "2", "3", "third_project"),
                Arrays.asList("2", "second", "1", "3", "third_project"),
                Arrays.asList("3", "third", "2", "3", "third_project"),
                Arrays.asList("4", "third", "3", "3", "third_project"),
                Arrays.asList("5", "fifth", "2", "3", "third_project")
        );
        assertThat(rowsAsValues(rows), equalTo(correctData));
    }

    private void doCommonAssertions(DataContainer dataContainer, int size) {
        assertThat(dataContainer.getColumnNames(), hasSize(5));
        assertThat(dataContainer.getColumnsMap().keySet(), contains(INSTANCES_ALIAS, PROJECTS_ALIAS));
        assertThat(dataContainer.getColumnsMap().get(INSTANCES_ALIAS), hasSize(3));
        assertThat(dataContainer.getColumnsMap().get(PROJECTS_ALIAS), hasSize(2));
        List<DataRow> rows = dataContainer.getRows();
        assertThat(rows, hasSize(size));
    }

    @Test
    public void testInnerJoinByCondition() throws Exception {
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.INNER);
            ds.setCondition(prepareJoinCondition());
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);
        doCommonAssertions(dataContainer, 5);
        
        List<DataRow> rows = dataContainer.getRows();
        List<List<Object>> correctData = Arrays.asList(
                Arrays.asList("2", "second", "1", "1", "first_project"),
                Arrays.asList("1", "first", "2", "2", "second_project"),
                Arrays.asList("3", "third", "2", "2", "second_project"),
                Arrays.asList("5", "fifth", "2", "2", "second_project"),
                Arrays.asList("4", "third", "3", "3", "third_project")
        );
        assertThat(rowsAsValues(rows), equalTo(correctData));
    }

    private static SimpleBooleanExpression prepareJoinCondition() {
        return new SimpleBooleanExpression(
                new ColumnExpression(PROJECT_ID, INSTANCES_ALIAS),
                BooleanRelation.EQUAL,
                new ColumnExpression(ID, PROJECTS_ALIAS)
        );
    }

    @Test
    public void testInnerJoinByColumns() throws Exception {
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.INNER);
            ds.getColumns().addAll(Collections.singletonList(ID));
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);
        doCommonAssertions(dataContainer, 3);

        List<DataRow> rows = dataContainer.getRows();
        List<List<Object>> correctData = Arrays.asList(
                Arrays.asList("1", "first", "2", "1", "first_project"),
                Arrays.asList("2", "second", "1", "2", "second_project"),
                Arrays.asList("3", "third", "2", "3", "third_project")
        );
        assertThat(rowsAsValues(rows), equalTo(correctData));
    }

    @Test
    public void testNaturalInnerJoin() throws Exception {
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.INNER);
            ds.setNaturalJoin(true);
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);

        doCommonAssertions(dataContainer, 3);
        List<DataRow> rows = dataContainer.getRows();
        List<List<Object>> correctData = Arrays.asList(
                Arrays.asList("1", "first", "2", "1", "first_project"),
                Arrays.asList("2", "second", "1", "2", "second_project"),
                Arrays.asList("3", "third", "2", "3", "third_project")
        );
        assertThat(rowsAsValues(rows), equalTo(correctData));
    }

    @Test
    public void testLeftJoinByCondition() throws Exception {
        dataFetcher.addDataRow(
                INSTANCES_TABLE,
                //There's no project with ID 4
                Arrays.asList("6", "sixth", "4")
        );
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.setCondition(prepareJoinCondition());
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);
        doCommonAssertions(dataContainer, 6);
        
        List<DataRow> rows = dataContainer.getRows();
        List<List<Object>> correctData = Arrays.asList(
                Arrays.asList("2", "second", "1", "1", "first_project"),
                Arrays.asList("1", "first", "2", "2", "second_project"),
                Arrays.asList("3", "third", "2", "2", "second_project"),
                Arrays.asList("5", "fifth", "2", "2", "second_project"),
                Arrays.asList("4", "third", "3", "3", "third_project"),
                Arrays.asList("6", "sixth", "4", null, null)
        );
        assertThat(rowsAsValues(rows), equalTo(correctData));
    }

    private void assertNaturalLeftJoinResults(DataContainer dataContainer) {
        doCommonAssertions(dataContainer, 5);

        List<DataRow> rows = dataContainer.getRows();
        List<List<Object>> correctData = Arrays.asList(
                Arrays.asList("1", "first", "2", "1", "first_project"),
                Arrays.asList("2", "second", "1", "2", "second_project"),
                Arrays.asList("3", "third", "2", "3", "third_project"),
                Arrays.asList("4", "third", "3", null, null),
                Arrays.asList("5", "fifth", "2", null, null)
        );
        assertThat(rowsAsValues(rows), equalTo(correctData));
    }

    @Test
    public void testLeftJoinByColumns() throws Exception {
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.getColumns().addAll(Collections.singletonList(ID));
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);
        assertNaturalLeftJoinResults(dataContainer);
    }

    @Test
    public void testRightJoinByCondition() throws Exception {
        dataFetcher.addDataRow(
                PROJECTS_TABLE,
                //There's no instance with project_id = 5
                Arrays.asList("5", "fifth_project")
        );
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.RIGHT);
            ds.setCondition(prepareJoinCondition());
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);
        doCommonAssertions(dataContainer, 6);
        List<DataRow> rows = dataContainer.getRows();
        List<List<Object>> correctData = Arrays.asList(
                Arrays.asList("2", "second", "1", "1", "first_project"),
                Arrays.asList("1", "first", "2", "2", "second_project"),
                Arrays.asList("3", "third", "2", "2", "second_project"),
                Arrays.asList("5", "fifth", "2", "2", "second_project"),
                Arrays.asList("4", "third", "3", "3", "third_project"),
                Arrays.asList(null, null, null, "5", "fifth_project")
        );
        assertThat(rowsAsValues(rows), equalTo(correctData));
    }

    @Test
    public void testNaturalLeftJoin() throws Exception {
        DataSource dataSource = prepareJoinDataSource(ds -> {
            ds.setJoinType(JoinType.LEFT);
            ds.setNaturalJoin(true);
            return ds;
        });
        DataSourceStrategy strategy = getStrategy();
        DataContainer dataContainer = strategy.process(dataSource, TWO_TABLE_ALIASES);
        assertNaturalLeftJoinResults(dataContainer);
    }

    private DataSource prepareJoinDataSource(Function<DataSource, DataSource> secondDataSourceProcessor) {
        DataSource first = new DataSource(INSTANCES_ALIAS);
        DataSource second = secondDataSourceProcessor.apply(new DataSource(PROJECTS_ALIAS));
        first.setRightDataSource(second);
        return first;
    }
    
}