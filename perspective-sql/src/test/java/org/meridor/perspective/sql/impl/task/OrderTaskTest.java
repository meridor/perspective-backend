package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.OrderDirection;
import org.meridor.perspective.sql.impl.expression.OrderExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class OrderTaskTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private static final String FIRST_COLUMN_TO_SORT_BY = "str";
    private static final String SECOND_COLUMN_TO_SORT_BY = "numWithDefaultValue";
    private static final String TABLE_NAME = "mock";
    
    private static ExecutionResult getInput() {
        ExecutionResult input = new ExecutionResult();
        input.setCount(4);
        List<DataRow> initialData = new ArrayList<>();
        initialData.add(createRow("b", 3));
        initialData.add(createRow("b", 2));
        initialData.add(createRow("a", 4));
        initialData.add(createRow("a", 3));
        input.setData(initialData);
        return input;
    }

    private static DataRow createRow(String first, Integer second) {
        return new DataRow(){
            {
                put(FIRST_COLUMN_TO_SORT_BY, first);
                put(SECOND_COLUMN_TO_SORT_BY, second);
            }
        };
    }
    
    @Test
    public void testExecute() throws Exception {
        OrderTask orderTask = applicationContext.getBean(OrderTask.class);
        orderTask.addExpression(new OrderExpression(new ColumnExpression(FIRST_COLUMN_TO_SORT_BY, TABLE_NAME)));
        orderTask.addExpression(new OrderExpression(new ColumnExpression(SECOND_COLUMN_TO_SORT_BY, TABLE_NAME), OrderDirection.DESC));
        ExecutionResult executionResult = orderTask.execute(getInput());
        assertThat(executionResult.getCount(), equalTo(4));
        List<DataRow> data = executionResult.getData();
        assertThat(data.get(0), equalTo(createRow("a", 4)));
        assertThat(data.get(1), equalTo(createRow("a", 3)));
        assertThat(data.get(2), equalTo(createRow("b", 3)));
        assertThat(data.get(3), equalTo(createRow("b", 2)));
    }
}