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

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class SelectTaskTest {

    private static final String TABLE_NAME = "mock";
    private static final String FIRST_COLUMN = "str";
    private static final String SECOND_COLUMN = "missingDefaultValue";
    private static final String FIRST_ALIAS = "first";
    private static final String SECOND_ALIAS = "second";
    private static final String THIRD_ALIAS = "third";

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testExecute() throws Exception {
        Map<String, Object> selectionMap = new HashMap<String, Object>(){
            {
                put(FIRST_ALIAS, new ColumnExpression(FIRST_COLUMN, TABLE_NAME));
                put(SECOND_ALIAS, new FunctionExpression("abs", Collections.singletonList(new ColumnExpression(SECOND_COLUMN, TABLE_NAME))));
                put(THIRD_ALIAS, 42);
            }
        };
        SelectTask selectTask = applicationContext.getBean(
                SelectTask.class,
                selectionMap
        );
        ExecutionResult inputData = getInput();
        ExecutionResult executionResult = selectTask.execute(inputData);
        assertThat(executionResult.getCount(), equalTo(2));
        List<DataRow> rows = executionResult.getData().getRows();
        assertThat(rows.size(), equalTo(2));
        assertThat(rows.get(0).getValues(), hasSize(3));
        assertThat(rows.get(0).get(FIRST_ALIAS), equalTo("one"));
        assertThat(rows.get(0).get(SECOND_ALIAS), equalTo(333d));
        assertThat(rows.get(0).get(THIRD_ALIAS), equalTo(42));
        assertThat(rows.get(1).getValues(), hasSize(3));
        assertThat(rows.get(1).get(FIRST_ALIAS), equalTo("two"));
        assertThat(rows.get(1).get(SECOND_ALIAS), equalTo(222d));
        assertThat(rows.get(1).get(THIRD_ALIAS), equalTo(42));
    }

    private static ExecutionResult getInput() {
        ExecutionResult input = new ExecutionResult();
        input.setCount(2);
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>() {
            {
                put(TABLE_NAME, Arrays.asList(FIRST_COLUMN, SECOND_COLUMN));
            }
        };
        DataContainer dataContainer = new DataContainer(columnsMap);
        dataContainer.addRow(createRow("one", -333L));
        dataContainer.addRow(createRow("two", 222L));
        input.setData(dataContainer);
        return input;
    }

    private static List<Object> createRow(String first, Long second) {
        return new ArrayList<Object>(){
            {
                add(first);
                add(second);
            }
        };
    }

}