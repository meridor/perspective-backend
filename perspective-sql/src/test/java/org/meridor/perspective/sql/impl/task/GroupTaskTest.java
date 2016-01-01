package org.meridor.perspective.sql.impl.task;

import javafx.application.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.FunctionExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class GroupTaskTest {

    private static final String TABLE_NAME = "mock";
    private static final String FIRST_COLUMN = "str";
    private static final String SECOND_COLUMN = "numWithDefaultValue";
    private static final String THIRD_COLUMN = "num";
    private static final String FUNCTION_NAME = "abs";
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void testExecute() throws Exception {
        GroupTask groupTask = applicationContext.getBean(GroupTask.class);
        groupTask.addExpression(new ColumnExpression(FIRST_COLUMN, TABLE_NAME));
        groupTask.addExpression(new FunctionExpression(
                FUNCTION_NAME,
                Collections.singletonList(new ColumnExpression(SECOND_COLUMN, TABLE_NAME))
        ));
        ExecutionResult output = groupTask.execute(createInput());
        assertThat(output.getCount(), equalTo(3));
        List<DataRow> data = output.getData();
        assertThat(data, hasSize(3));
        assertThat(data, containsInAnyOrder(
                createRow("one", 1),
                createRow("two", 1),
                createRow("two", 2)
        ));
    }
    
    private ExecutionResult createInput() {
        ExecutionResult executionResult = new ExecutionResult();
        List<DataRow> data = new ArrayList<>();
        data.add(createRow("one", 1));
        data.add(createRow("two", 1));
        data.add(createRow("two", 2));
        data.add(createRow("two", -2));
        executionResult.setData(data);
        return executionResult;
    }
    
    private DataRow createRow(String first, Integer second) {
        return new DataRow() {
            {
                put(FIRST_COLUMN, first);
                put(SECOND_COLUMN, second);
            }
        };
    }
}