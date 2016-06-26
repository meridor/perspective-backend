package org.meridor.perspective.sql.impl;

import org.junit.Ignore;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.sql.SQLSyntaxErrorException;
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
@Ignore
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
        testSelect(selectionMap, selectionMap, false);
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
        testSelect(selectionMap, effectiveSelectionMap, false);
    }
    
    @Test
    public void testSelectAll() throws Exception {
        Map<String, Object> selectionMap = Collections.singletonMap("any_alias", new ColumnExpression());
        testSelect(selectionMap, Collections.emptyMap(), true);
    }
    
    private void testSelect(Map<String, Object> selectionMap, Map<String, Object> effectiveSelectionMap, boolean isSelectAll) throws Exception {
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().putAll(selectionMap);
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
    
    private Queue<Task> plan() throws SQLSyntaxErrorException {
        return queryPlanner.plan(STUB_SQL);
    }

    @Test
    public void testTableScanStrategySimpleFetch() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES);
        DataSource parentDataSource = new DataSource(leftDataSource);
        parentDataSource.setType(PARENT);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(ID, new ColumnExpression(ID, INSTANCES));
                setDataSource(parentDataSource);
                //No where clause is present
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks, 2);
        assertThat(dataSourceTask.getDataSource(), equalTo(parentDataSource));
    }

    @Test
    public void testIndexScanStrategySimpleFetch() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES_ALIAS);
        DataSource parentDataSource = new DataSource(leftDataSource);
        parentDataSource.setType(PARENT);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(ID, new ColumnExpression(ID, INSTANCES));
                setDataSource(parentDataSource);
                BooleanExpression whereCondition = new SimpleBooleanExpression(new ColumnExpression(NAME, INSTANCES_ALIAS), EQUAL, VALUE);
                setWhereExpression(whereCondition); //Where clause columns are from index but select map contains not indexed columns
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks, 2);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource.getType(), equalTo(INDEX_SCAN));
        assertThat(optimizedLeftDataSource.getCondition().isPresent(), is(true));
        
        BooleanExpression optimizedCondition = optimizedLeftDataSource.getCondition().get();
        assertThat(optimizedCondition, is(instanceOf(IndexBooleanExpression.class)));
        assertThat(optimizedCondition.getTableAliases(), contains(INSTANCES_ALIAS));
        assertThat(optimizedCondition.getColumnRelations().keySet(), is(empty()));
        assertThat(optimizedCondition.getRestOfExpression().isPresent(), is(false));
        assertThat(optimizedCondition.getFixedValueConditions(INSTANCES_ALIAS), equalTo(Collections.singletonMap(NAME, VALUE)));
    }

    @Test
    public void testIndexScanStrategyInnerJoin() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES_ALIAS);
        DataSource rightDataSource = new DataSource(PROJECTS_ALIAS);
        rightDataSource.setJoinType(INNER);
        BooleanExpression joinCondition = new SimpleBooleanExpression(new ColumnExpression(PROJECT_ID, INSTANCES_ALIAS), EQUAL, new ColumnExpression(ID, PROJECTS_ALIAS));
        rightDataSource.setCondition(joinCondition);
        DataSource parentDataSource = new DataSource(leftDataSource);
        parentDataSource.setType(PARENT);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(NAME, new ColumnExpression(NAME, INSTANCES));
                getSelectionMap().put(PROJECT_ID, new ColumnExpression(ID, PROJECTS));
                setDataSource(parentDataSource);
                BooleanExpression whereCondition = new SimpleBooleanExpression(new ColumnExpression(NAME, INSTANCES_ALIAS), EQUAL, VALUE);
                setWhereExpression(whereCondition);
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks, 2);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource.getTableAlias(), equalTo(INSTANCES_ALIAS));
        assertThat(optimizedLeftDataSource.getType(), equalTo(INDEX_SCAN));
        assertThat(optimizedLeftDataSource.getCondition().isPresent(), is(true));
        assertThat(optimizedLeftDataSource.getColumns(), contains(PROJECT_ID));
        
        assertThat(dataSourceTask.getDataSource().getRightDataSource().isPresent(), is(true));
        DataSource optimizedRightDataSource = dataSourceTask.getDataSource().getRightDataSource().get();
        assertThat(optimizedRightDataSource.getTableAlias(), equalTo(PROJECTS_ALIAS));
        assertThat(optimizedRightDataSource.getType(), equalTo(INDEX_SCAN));
        assertThat(optimizedRightDataSource.getColumns(), contains(ID));

        BooleanExpression optimizedLeftCondition = optimizedLeftDataSource.getCondition().get();
        assertThat(optimizedLeftCondition, is(instanceOf(IndexBooleanExpression.class)));
        assertThat(optimizedLeftCondition.getTableAliases(), containsInAnyOrder(INSTANCES_ALIAS, PROJECTS_ALIAS));
        assertThat(optimizedLeftCondition.getColumnRelations().keySet(), is(empty()));
        assertThat(optimizedLeftCondition.getRestOfExpression().isPresent(), is(false));
        assertThat(optimizedLeftCondition.getFixedValueConditions(INSTANCES_ALIAS), equalTo(Collections.singletonMap(NAME, VALUE)));
    }
    
    @Test
    public void testIndexFetchStrategySimpleFetch() throws Exception {
        DataSource leftDataSource = new DataSource(INSTANCES_ALIAS);
        DataSource parentDataSource = new DataSource(leftDataSource);
        parentDataSource.setType(PARENT);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(NAME, new ColumnExpression(NAME, INSTANCES));
                setDataSource(leftDataSource);
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES); //All select map columns are from index
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks, 2);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource.getType(), equalTo(INDEX_FETCH));
        assertThat(optimizedLeftDataSource.getColumns(), contains(NAME));
    }
    
    private DataSourceTask doCommonTaskAssertions(List<Task> tasks, int size) {
        assertThat(tasks, hasSize(size));
        Task firstTask = tasks.get(0);
        assertThat(firstTask, is(instanceOf(DataSourceTask.class)));
        assertThat(tasks.get(1), is(instanceOf(SelectTask.class)));
        return (DataSourceTask) firstTask;
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
        DataSource parentDataSource = new DataSource(leftDataSource);
        parentDataSource.setType(PARENT);
        queryParser.setSelectQueryAware(new MockSelectQueryAware(){
            {
                getSelectionMap().put(ID, new ColumnExpression(ID, INSTANCES));
                setDataSource(parentDataSource);
                getTableAliases().put(INSTANCES_ALIAS, INSTANCES);
                BooleanExpression whereCondition = new BinaryBooleanExpression(
                        new SimpleBooleanExpression(new ColumnExpression(NAME, INSTANCES_ALIAS), EQUAL, VALUE),
                        OR,
                        new SimpleBooleanExpression(new ColumnExpression(NAME, INSTANCES_ALIAS), EQUAL, ANOTHER_VALUE)
                );
                setWhereExpression(whereCondition);
            }
        });
        List<Task> tasks = new ArrayList<>(plan());
        DataSourceTask dataSourceTask = doCommonTaskAssertions(tasks, 2);
        DataSource optimizedLeftDataSource = doOptimizedLeftDataSourceAssertions(dataSourceTask);
        assertThat(optimizedLeftDataSource.getCondition().isPresent(), is(true));
        BooleanExpression optimizedLeftCondition = optimizedLeftDataSource.getCondition().get();
        assertThat(optimizedLeftCondition.getTableAliases(), contains(INSTANCES_ALIAS));
        Map<String, Set<Object>> fixedValueCondition = optimizedLeftCondition.getFixedValueConditions(INSTANCES_ALIAS);
        assertThat(fixedValueCondition.keySet(), contains(NAME));
        assertThat(fixedValueCondition.get(NAME), containsInAnyOrder(VALUE, ANOTHER_VALUE));
    }
}