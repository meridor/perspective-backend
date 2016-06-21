package org.meridor.perspective.sql.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.OrderDirection;
import org.meridor.perspective.sql.impl.expression.OrderExpression;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.task.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLSyntaxErrorException;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.table.Column.ANY;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class QueryPlannerImplTest {

    private static final String STUB_SQL = "stub";
    
    @Autowired
    private QueryPlanner queryPlanner;
    
    @Autowired
    private MockQueryParser queryParser;
    
    @Autowired
    private IndexStorage indexStorage;
    
    @Test
    public void testSelectExpression() throws Exception {
        Map<String, Object> selectionMap = Collections.singletonMap("alias", "one");
        testSelect(selectionMap, selectionMap, false);
    }
    
    @Test
    public void testSelectAllFromTable() throws Exception {
        Map<String, Object> selectionMap = Collections.singletonMap("instances", new ColumnExpression(ANY, "instances"));
        Map<String, Object> effectiveSelectionMap = new LinkedHashMap<String, Object>(){
            {
                put("id", new ColumnExpression("id", "instances"));
                put("name", new ColumnExpression("name", "instances"));
                put("project_id", new ColumnExpression("project_id", "instances"));
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
        ColumnExpression groupByExpression = new ColumnExpression("name", "instances");
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
        OrderExpression orderByExpression = new OrderExpression(new ColumnExpression("name", "instances"), OrderDirection.DESC);
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
    
}