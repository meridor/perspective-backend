package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.FunctionExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class GroupTaskTest {

    private static final String TABLE_NAME = "mock";
    private static final String FIRST_COLUMN = "str";
    private static final String SECOND_COLUMN = "numWithDefaultValue";
    private static final String FUNCTION_NAME = "abs";
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    public void testExecute() throws Exception {
        ExecutionResult output = groupBy(false);
        assertThat(output.getCount(), equalTo(3));
        List<DataRow> data = output.getData().getRows();
        assertThat(data, hasSize(3));
        List<List<?>> dataRows = data.stream()
                .map(DataRow::getValues)
                .collect(Collectors.toList());
        assertThat(dataRows, containsInAnyOrder(
                createRow("one", 1),
                createRow("two", 1),
                createRow("two", 2)
        ));
    }

    private ExecutionResult groupBy(boolean withNullRow) throws Exception {
        GroupTask groupTask = applicationContext.getBean(GroupTask.class);
        groupTask.addExpression(new ColumnExpression(FIRST_COLUMN, TABLE_NAME));
        groupTask.addExpression(new FunctionExpression(
                FUNCTION_NAME,
                Collections.singletonList(new ColumnExpression(SECOND_COLUMN, TABLE_NAME))
        ));
        return groupTask.execute(createInput(withNullRow));
    }

    @Test(expected = SQLException.class)
    public void testNullValue() throws Exception {
        groupBy(true);
    }

    private ExecutionResult createInput(boolean withNullRow) {
        ExecutionResult executionResult = new ExecutionResult();
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>() {
            {
                put(TABLE_NAME, Arrays.asList(FIRST_COLUMN, SECOND_COLUMN));
            }
        };
        DataContainer dataContainer = new DataContainer(columnsMap);
        dataContainer.addRow(createRow("one", 1));
        dataContainer.addRow(createRow("two", 1));
        dataContainer.addRow(createRow("two", 2));
        dataContainer.addRow(createRow("two", -2));
        if (withNullRow) {
            dataContainer.addRow(createRow(null, -3));
        }
        executionResult.setData(dataContainer);
        return executionResult;
    }
    
    private List<Object> createRow(String first, Integer second) {
        return new ArrayList<Object>() {
            {
                add(first);
                add(second);
            }
        };
    }
}