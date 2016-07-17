package org.meridor.perspective.sql.impl.task.strategy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.expression.BinaryBooleanExpression;
import org.meridor.perspective.sql.impl.expression.IndexBooleanExpression;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.HashTableIndex;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.parser.JoinType;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.task.MockDataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.index.Keys.key;
import static org.meridor.perspective.sql.impl.parser.JoinType.*;
import static org.meridor.perspective.sql.impl.task.strategy.StrategyTestUtils.*;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class IndexScanStrategyTest {
    
    @Autowired
    private MockDataFetcher dataFetcher;
    
    @Autowired
    private IndexStorage indexStorage;

    @Autowired
    private ApplicationContext applicationContext;

    private static final Index INSTANCES_NAME_INDEX = new HashTableIndex(new IndexSignature(INSTANCES, Collections.singleton(NAME))){
        {
            put(key("first"), "1");
            put(key("second"), "2");
            put(key("third"), "3");
            put(key("third"), "4");
            put(key("firth"), "2");
        }
    };
    private static final Index INSTANCES_PROJECT_ID_INDEX = new HashTableIndex(new IndexSignature(INSTANCES, Collections.singleton(PROJECT_ID))){
        {
            put(key("2"), "1");
            put(key("1"), "2");
            put(key("2"), "3");
            put(key("3"), "4");
            put(key("2"), "5");
        }
    };
    private static final Index PROJECTS_ID_INDEX = new HashTableIndex(new IndexSignature(PROJECTS, Collections.singleton(ID))){
        {
            put(key("1"), "1");
            put(key("2"), "2");
            put(key("3"), "3");
        }
    };


    @Before
    public void init() {
        dataFetcher.setTableData(INSTANCES, INSTANCES_COLUMNS, INSTANCES_DATA);
        dataFetcher.setTableData(PROJECTS, PROJECTS_COLUMNS, PROJECTS_DATA);
        indexStorage.put(INSTANCES_NAME_INDEX.getSignature(), INSTANCES_NAME_INDEX);
        indexStorage.put(INSTANCES_PROJECT_ID_INDEX.getSignature(), INSTANCES_PROJECT_ID_INDEX);
        indexStorage.put(PROJECTS_ID_INDEX.getSignature(), PROJECTS_ID_INDEX);
    }
    
    @Test
    public void testFetch() {
        IndexBooleanExpression expression = new IndexBooleanExpression(
                INSTANCES_ALIAS,
                new HashMap<String, Set<Object>>() {
                    {
                        put(NAME, Collections.singleton("third"));
                        put(PROJECT_ID, Collections.singleton("2"));
                    }
                }
        );
        DataSource dataSource = new DataSource(INSTANCES_ALIAS);
        dataSource.setCondition(expression);

        DataSourceStrategy strategy = getStrategy();
        DataContainer result = strategy.process(dataSource, TWO_TABLE_ALIASES);
        assertThat(result.getColumnsMap(),  equalTo(Collections.singletonMap(INSTANCES_ALIAS, INSTANCES_COLUMNS)));
        assertThat(result.getRows(), hasSize(1));
        assertThat(result.getRows().get(0).getValues(), contains("3", "third", "2"));
    }
    
    @Test
    public void testInnerJoin() {
        DataSource dataSource = prepareJoinDataSource(INNER);
        DataSourceStrategy strategy = getStrategy();
        DataContainer result = strategy.process(dataSource, TWO_TABLE_ALIASES);
        assertContainerColumns(result);
        assertThat(result.getRows(), hasSize(1));
        assertThat(result.getRows().get(0).getValues(), contains("1", "first", "2", "2", "second_project"));
    }
    
    @Test
    public void testLeftJoin() {
        DataSource dataSource = prepareJoinDataSource(LEFT);
        DataSourceStrategy strategy = getStrategy();
        DataContainer result = strategy.process(dataSource, TWO_TABLE_ALIASES);
        assertContainerColumns(result);
        assertThat(result.getRows(), hasSize(5));
        assertThat(result.getRows().get(0).getValues(), contains("1", "first", "2", "2", "second_project"));
        assertThat(result.getRows().get(1).getValues(), contains("2", "second", "1", null, null));
        assertThat(result.getRows().get(2).getValues(), contains("3", "third", "2", null, null));
        assertThat(result.getRows().get(3).getValues(), contains("4", "third", "3", null, null));
        assertThat(result.getRows().get(4).getValues(), contains("5", "fifth", "2", null, null));
    }
    
    @Test
    public void testRightJoin() {
        DataSource dataSource = prepareJoinDataSource(RIGHT);
        DataSourceStrategy strategy = getStrategy();
        DataContainer result = strategy.process(dataSource, TWO_TABLE_ALIASES);
        assertContainerColumns(result);
        assertThat(result.getRows(), hasSize(3));
        assertThat(result.getRows().get(0).getValues(), contains("1", "first", "2", "2", "second_project"));
        assertThat(result.getRows().get(1).getValues(), contains(null, null, null, "1", "first_project"));
        assertThat(result.getRows().get(2).getValues(), contains(null, null, null, "3", "third_project"));
    }
    
    private void assertContainerColumns(DataContainer result) {
        Map<String, List<String>> columnsMap = result.getColumnsMap();
        assertThat(columnsMap.keySet(), containsInAnyOrder(INSTANCES_ALIAS, PROJECTS_ALIAS));
        String[] instancesColumnsArray = INSTANCES_COLUMNS.toArray(new String[INSTANCES_COLUMNS.size()]);
        assertThat(columnsMap.get(INSTANCES_ALIAS), containsInAnyOrder(instancesColumnsArray));
        String[] projectsColumnsArray = PROJECTS_COLUMNS.toArray(new String[PROJECTS_COLUMNS.size()]);
        assertThat(columnsMap.get(PROJECTS_ALIAS), containsInAnyOrder(projectsColumnsArray));
    }
    
    private DataSource prepareJoinDataSource(JoinType joinType) {
        IndexBooleanExpression instancesExpression = new IndexBooleanExpression(
                INSTANCES_ALIAS,
                new HashMap<String, Set<Object>>() {
                    {
                        put(NAME, Collections.singleton("first"));
                    }
                }
        );
        DataSource instancesDataSource = new DataSource(INSTANCES_ALIAS);
        instancesDataSource.setCondition(instancesExpression);
        instancesDataSource.getColumns().add(PROJECT_ID);

        DataSource projectsDataSource = new DataSource(PROJECTS_ALIAS);
        projectsDataSource.setCondition(IndexBooleanExpression.empty());
        projectsDataSource.getColumns().add(ID);
        projectsDataSource.setJoinType(joinType);
        instancesDataSource.setRightDatasource(projectsDataSource);
        return instancesDataSource;
    }
    

    @Test(expected = IllegalArgumentException.class)
    public void testNoTableAlias() {
        DataSource dataSource = new DataSource(new DataSource(TABLE_ALIAS));
        assertThat(dataSource.getTableAlias().isPresent(), is(false));
        DataSourceStrategy strategy = getStrategy();
        strategy.process(dataSource, TABLE_ALIASES);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWrongConditionType() {
        DataSource dataSource = new DataSource(TABLE_ALIAS);
        dataSource.setCondition(BinaryBooleanExpression.alwaysTrue());
        assertThat(dataSource.getCondition().isPresent(), is(true));
        DataSourceStrategy strategy = getStrategy();
        strategy.process(dataSource, TABLE_ALIASES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoreThanTwoDataSources() {
        DataSource firstDataSource = new DataSource(TABLE_ALIAS);
        firstDataSource.setCondition(new IndexBooleanExpression(TABLE_ALIAS, Collections.emptyMap()));
        DataSource secondDataSource = new DataSource(TABLE_ALIAS);
        DataSource thirdDataSource = new DataSource(TABLE_ALIAS);
        secondDataSource.setRightDatasource(thirdDataSource);
        firstDataSource.setRightDatasource(secondDataSource);
        DataSourceStrategy strategy = getStrategy();
        strategy.process(firstDataSource, TABLE_ALIASES);
    }
    
    private DataSourceStrategy getStrategy() {
        return applicationContext.getBean(IndexScanStrategy.class);
    }

}