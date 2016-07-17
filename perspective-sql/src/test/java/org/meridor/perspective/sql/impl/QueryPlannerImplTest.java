package org.meridor.perspective.sql.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.impl.expression.*;
import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.HashTableIndex;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.parser.DataSource;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.task.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.BooleanRelation.EQUAL;
import static org.meridor.perspective.sql.impl.expression.BinaryBooleanOperator.OR;
import static org.meridor.perspective.sql.impl.parser.DataSource.DataSourceType.*;
import static org.meridor.perspective.sql.impl.parser.JoinType.INNER;
import static org.meridor.perspective.sql.impl.table.Column.ANY;
import static org.meridor.perspective.sql.impl.task.strategy.StrategyTestUtils.*;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class QueryPlannerImplTest {

    private static final String STUB_SQL = "stub";

    private static final String VALUE = "value";
    private static final String ANOTHER_VALUE = "another_value";

    @Autowired
    private QueryPlanner queryPlanner;
    
    @Autowired
    private MockQueryParser queryParser;
    
    @Autowired
    private IndexStorage indexStorage;
    
    @PostConstruct
    public void init() {
        Index instancesNameIndex = createInstancesNameIndex();
        indexStorage.put(instancesNameIndex.getSignature(), instancesNameIndex);
        
        Index instancesProjectIdIndex = createInstancesProjectIdIndex();
        indexStorage.put(instancesProjectIdIndex.getSignature(), instancesProjectIdIndex);
        
        Index projectsIdIndex = createProjectsIdIndex();
        indexStorage.put(projectsIdIndex.getSignature(), projectsIdIndex);
    }
    
    private Index createInstancesNameIndex() {
        IndexSignature indexSignature = new IndexSignature(INSTANCES, Collections.singleton(NAME));
        return new HashTableIndex(indexSignature);
    }
    
    private Index createInstancesProjectIdIndex() {
        IndexSignature indexSignature = new IndexSignature(INSTANCES, Collections.singleton(PROJECT_ID));
        return new HashTableIndex(indexSignature);
    }
    
    private Index createProjectsIdIndex() {
        IndexSignature indexSignature = new IndexSignature(PROJECTS, Collections.singleton(ID));
        return new HashTableIndex(indexSignature);
    }
    
    @Test
    public void testSelectExpression() throws Exception {
        Map<String, Object> selectionMap = Collections.singletonMap(INSTANCES_ALIAS, "one");
        testSelect(selectionMap, selectionMap, Collections.singletonMap(INSTANCES_ALIAS, INSTANCES), false);
    }
    
    @Test
    public void testSelectAllFromTable() throws Exception {
        Map<String, Object> selectionMap = Collections.singletonMap(INSTANCES, new ColumnExpression(ANY, INSTANCES));
        Map<String, Object> effectiveSelectionMap = new LinkedHashMap<String, Object>(){
            {
                put(ID, new ColumnExpression(ID, INSTANCES));
                put(NAME, new ColumnExpression(NAME, INSTANCES));
                put(PROJECT_ID, new ColumnExpression(PROJECT_ID, INSTANCES));
            }
        };
        testSelect(selectionMap, effectiveSelectionMap, Collections.singletonMap(INSTANCES, INSTANCES), false);
    }
    
    @Test
    public void testSelectAll() throws Exception {
        Map<String, Object> selectionMap = Collections.singletonMap(ANY, new ColumnExpression());
        testSelect(selectionMap, Collections.emptyMap(), Collections.singletonMap(INSTANCES_ALIAS, INSTANCES), true);
    }
    
    private void testSelect(Map<String, Object> selectionMap, Map<String, Object> effectiveSelectionMap, Map<String, String> tableAliases, boolean isSelectAll) throws Exception {
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().putAll(selectionMap);
                getTableAliases().putAll(tableAliases);
            }
        });
        Queue<Task> tasks = plan();
        assertThat(tasks, hasSize(2));
        Task firstTask = tasks.poll();
        assertThat(firstTask, is(instanceOf(DummyFetchTask.class)));
        Task secondTask = tasks.poll();
        assertThat(secondTask, is(instanceOf(SelectTask.class)));
        SelectTask selectTask = (SelectTask) secondTask;
        assertThat(selectTask.getSelectionMap(), equalTo(effectiveSelectionMap));
        assertThat(selectTask.isSelectAll(), is(isSelectAll));
        
    }
    
    @Test
    public void testGroupBy() throws Exception {
        ColumnExpression groupByExpression = new ColumnExpression(NAME, INSTANCES);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getGroupByExpressions().add(groupByExpression);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        assertThat(tasks, hasSize(3)); //DummyFetchTask, GroupTask, SelectTask
        Task secondTask = tasks.get(1);
        assertThat(secondTask, is(instanceOf(GroupTask.class)));
        GroupTask groupTask = (GroupTask) secondTask;
        assertThat(groupTask.getExpressions(), contains(groupByExpression));
    }
    
    @Test
    public void testOrderBy() throws Exception {
        OrderExpression orderByExpression = new OrderExpression(new ColumnExpression(NAME, INSTANCES), OrderDirection.DESC);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getOrderByExpressions().add(orderByExpression);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        assertThat(tasks, hasSize(3)); //DummyFetchTask, OrderTask, SelectTask
        Task secondTask = tasks.get(1);
        assertThat(secondTask, is(instanceOf(OrderTask.class)));
        OrderTask orderTask = (OrderTask) secondTask;
        assertThat(orderTask.getExpressions(), contains(orderByExpression));
    }
    
    @Test
    public void testLimit() throws Exception {
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                setLimitCount(10);
                setLimitOffset(20);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        assertThat(tasks, is(not(empty())));
        Task lastTask = tasks.get(tasks.size() - 1);
        assertThat(lastTask, is(instanceOf(LimitTask.class)));
        LimitTask limitTask = (LimitTask) lastTask;
        assertThat(limitTask.getCount(), equalTo(10));
        assertThat(limitTask.getOffset(), equalTo(20));
    }
    
    private Queue<Task> plan() throws SQLException {
        return queryPlanner.plan(STUB_SQL);
    }

    @Test
    public void testTableScanStrategySimpleFetch() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(ID, new ColumnExpression(ID, INSTANCES));
                setDataSource(leftDataSource);
                //No where clause is present
                getTableAliases().put(INSTANCES, INSTANCES);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource, equalTo(leftDataSource));
    }

    @Test
    public void testIndexScanStrategySimpleFetch() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES_ALIAS);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(ID, new ColumnExpression(ID, INSTANCES_ALIAS));
                setDataSource(leftDataSource);
                BooleanExpression whereCondition = new SimpleBooleanExpression(new ColumnExpression(NAME, INSTANCES_ALIAS), EQUAL, VALUE);
                setWhereExpression(whereCondition); //Where clause columns are from index but select map contains not indexed columns
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource.getType(), equalTo(INDEX_SCAN));
        assertThat(optimizedLeftDataSource.getCondition().isPresent(), is(true));
        
        BooleanExpression optimizedCondition = optimizedLeftDataSource.getCondition().get();
        assertThat(optimizedCondition, is(instanceOf(IndexBooleanExpression.class)));
        assertThat(optimizedCondition.getTableAliases(), contains(INSTANCES_ALIAS));
        assertThat(optimizedCondition.getColumnRelations().isPresent(), is(false));
        assertThat(optimizedCondition.getRestOfExpression().isPresent(), is(false));
        assertThat(optimizedCondition.getFixedValueConditions(INSTANCES_ALIAS), equalTo(Collections.singletonMap(NAME, Collections.singleton(VALUE))));
    }

    @Test
    public void testIndexScanStrategyInnerJoin() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES_ALIAS);
        DataSource rightDataSource = new DataSource(PROJECTS_ALIAS);
        rightDataSource.setJoinType(INNER);
        BooleanExpression joinCondition = new SimpleBooleanExpression(new ColumnExpression(PROJECT_ID, INSTANCES_ALIAS), EQUAL, new ColumnExpression(ID, PROJECTS_ALIAS));
        rightDataSource.setCondition(joinCondition);
        leftDataSource.setRightDatasource(rightDataSource);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(NAME, new ColumnExpression(NAME, INSTANCES_ALIAS));
                getSelectionMap().put(PROJECT_ID, new ColumnExpression(ID, PROJECTS_ALIAS));
                setDataSource(leftDataSource);
                BooleanExpression whereCondition = new SimpleBooleanExpression(new ColumnExpression(NAME, INSTANCES_ALIAS), EQUAL, VALUE);
                setWhereExpression(whereCondition);
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES);
                getTableAliases().put(PROJECTS_ALIAS, PROJECTS);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource.getTableAlias().isPresent(), is(true));
        assertThat(optimizedLeftDataSource.getTableAlias().get(), equalTo(INSTANCES_ALIAS));
        assertThat(optimizedLeftDataSource.getType(), equalTo(INDEX_SCAN));
        assertThat(optimizedLeftDataSource.getCondition().isPresent(), is(true));
        assertThat(optimizedLeftDataSource.getColumns(), contains(PROJECT_ID));
        
        assertThat(optimizedLeftDataSource.getRightDataSource().isPresent(), is(true));
        DataSource optimizedRightDataSource = optimizedLeftDataSource.getRightDataSource().get();
        assertThat(optimizedRightDataSource.getTableAlias().isPresent(), is(true));
        assertThat(optimizedRightDataSource.getTableAlias().get(), equalTo(PROJECTS_ALIAS));
        assertThat(optimizedRightDataSource.getType(), equalTo(INDEX_SCAN));
        assertThat(optimizedRightDataSource.getColumns(), contains(ID));

        BooleanExpression optimizedLeftCondition = optimizedLeftDataSource.getCondition().get();
        assertThat(optimizedLeftCondition, is(instanceOf(IndexBooleanExpression.class)));
        assertThat(optimizedLeftCondition.getTableAliases(), contains(INSTANCES_ALIAS));
        assertThat(optimizedLeftCondition.getColumnRelations().isPresent(), is(false));
        assertThat(optimizedLeftCondition.getRestOfExpression().isPresent(), is(false));
        assertThat(optimizedLeftCondition.getFixedValueConditions(INSTANCES_ALIAS), equalTo(Collections.singletonMap(NAME, Collections.singleton(VALUE))));
    }
    
    @Test
    public void testIndexFetchStrategySimpleFetch() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES_ALIAS);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(NAME, new ColumnExpression(NAME, INSTANCES_ALIAS));
                setDataSource(leftDataSource);
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES); //All select map columns are from index
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource.getType(), equalTo(INDEX_FETCH));
        assertThat(optimizedLeftDataSource.getColumns(), contains(NAME));
    }
    
    private DataSourceTask doCommonTaskAssertions(List<Task> tasks, List<Class<?>> taskClasses) {
        assertThat(tasks, hasSize(taskClasses.size()));
        Task firstTask = tasks.get(0);
        int counter = 0;
        for (Class<?> taskClass : taskClasses) {
            assertThat(tasks.get(counter), is(instanceOf(taskClass)));
            counter++;
        }
        return (DataSourceTask) firstTask;
    }

    private DataSourceTask doCommonTaskAssertions(List<Task> tasks) {
        return doCommonTaskAssertions(tasks, Arrays.asList(DataSourceTask.class, SelectTask.class));
    }


    private DataSource doOptimizedLeftDataSourceAssertions(DataSourceTask dataSourceTask) {
        DataSource optimizedDataSource = dataSourceTask.getDataSource();
        assertThat(optimizedDataSource.getType(), equalTo(PARENT));
        assertThat(optimizedDataSource.getLeftDataSource().isPresent(), is(true));
        return optimizedDataSource.getLeftDataSource().get();
    }
    
    @Test
    public void testOptimizeMultipleOrConditions() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES_ALIAS);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(NAME, new ColumnExpression(NAME, INSTANCES_ALIAS));
                setDataSource(leftDataSource);
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES);
                BooleanExpression whereCondition = new BinaryBooleanExpression(
                        new SimpleBooleanExpression(new ColumnExpression(ID, INSTANCES_ALIAS), EQUAL, VALUE),
                        OR,
                        new SimpleBooleanExpression(new ColumnExpression(ID, INSTANCES_ALIAS), EQUAL, ANOTHER_VALUE)
                );
                setWhereExpression(whereCondition);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks, Arrays.asList(DataSourceTask.class, FilterTask.class, SelectTask.class));
        doOptimizedLeftDataSourceAssertions(dataSourceTask);
        FilterTask filterTask = (FilterTask) tasks.get(1);
        assertThat(filterTask.getCondition().isPresent(), is(true));
        BooleanExpression optimizedLeftCondition = filterTask.getCondition().get();
        assertThat(optimizedLeftCondition.getTableAliases(), contains(INSTANCES_ALIAS));
        Map<String, Set<Object>> fixedValueCondition = optimizedLeftCondition.getFixedValueConditions(INSTANCES_ALIAS);
        assertThat(fixedValueCondition.keySet(), contains(ID));
        assertThat(fixedValueCondition.get(ID), containsInAnyOrder(VALUE, ANOTHER_VALUE));
    }
}