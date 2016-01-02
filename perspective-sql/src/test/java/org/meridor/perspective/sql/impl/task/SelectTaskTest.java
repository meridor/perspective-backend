package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class SelectTaskTest {

    private static final String TABLE_NAME = "mock";
    private static final String FIRST_COLUMN = "id";
    private static final String SECOND_COLUMN = "name";
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void testExecute() throws Exception {
        SelectTask selectTask = applicationContext.getBean(
                SelectTask.class,
                TABLE_NAME,
                Arrays.asList(FIRST_COLUMN, SECOND_COLUMN)
        );
        ExecutionResult executionResult = selectTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(2)); //Mock storage always returns 2 rows
        List<DataRow> data = executionResult.getData();
        assertThat(data.size(), equalTo(2));
        assertThat(data.get(0).keySet(), hasSize(2));
        assertThat(data.get(0).get(FIRST_COLUMN), equalTo(1));
        assertThat(data.get(0).get(SECOND_COLUMN), equalTo(1));
        assertThat(data.get(1).keySet(), hasSize(2));
        assertThat(data.get(1).get(FIRST_COLUMN), equalTo(2));
        assertThat(data.get(1).get(SECOND_COLUMN), equalTo(2));
    }
    
    @Test
    public void testSelectAll() throws Exception {
        SelectTask selectTask = applicationContext.getBean(
                SelectTask.class,
                TABLE_NAME,
                Collections.emptyList()
        );
        ExecutionResult executionResult = selectTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(2));
        List<DataRow> data = executionResult.getData();
        assertThat(data.get(0).keySet(), hasSize(4)); //All columns were selected
    }
    
    @Test
    public void testExecuteWithPredicate() throws Exception {
        SelectTask selectTask = applicationContext.getBean(
                SelectTask.class,
                TABLE_NAME,
                Arrays.asList(FIRST_COLUMN, SECOND_COLUMN)
        );
        selectTask.setCondition(r -> new Integer(2).equals(r.get(FIRST_COLUMN)));
        ExecutionResult executionResult = selectTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(1));
        List<DataRow> data = executionResult.getData();
        assertThat(data.size(), equalTo(1));
        assertThat(data.get(0).keySet(), hasSize(2));
        assertThat(data.get(0).get(FIRST_COLUMN), equalTo(2));
        assertThat(data.get(0).get(SECOND_COLUMN), equalTo(2));
    }
    
    @Test
    public void testMissingTable() throws Exception {
        SelectTask selectTask = applicationContext.getBean(SelectTask.class, "missing", Collections.emptyList());
        ExecutionResult executionResult = selectTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(0));
        assertThat(executionResult.getData(), is(empty()));
    }
    
}