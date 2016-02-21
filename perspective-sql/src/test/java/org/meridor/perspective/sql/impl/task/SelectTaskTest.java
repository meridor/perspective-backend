package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.expression.ColumnExpression;
import org.meridor.perspective.sql.impl.expression.FunctionExpression;
import org.meridor.perspective.sql.impl.table.Column;
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
    private static final String SECOND_COLUMN = "num";
    private static final String THIRD_COLUMN = "numWithDefaultValue";
    private static final String FOURTH_COLUMN = "missingDefaultValue";
    private static final String FIRST_ALIAS = "first";
    private static final String SECOND_ALIAS = "second";
    private static final String THIRD_ALIAS = "third";

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testSimpleSelect() throws Exception {
        Map<String, Object> selectionMap = new HashMap<String, Object>(){
            {
                put(FIRST_ALIAS, new ColumnExpression(FIRST_COLUMN, TABLE_NAME));
                put(SECOND_ALIAS, new FunctionExpression("abs", Collections.singletonList(new ColumnExpression(FOURTH_COLUMN, TABLE_NAME))));
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

        DataRow firstRow = rows.get(0);
        assertThat(firstRow.getValues(), hasSize(3));
        assertThat(firstRow.get(FIRST_ALIAS), equalTo("one"));
        assertThat(firstRow.get(SECOND_ALIAS), equalTo(333d));
        assertThat(firstRow.get(THIRD_ALIAS), equalTo(42));

        DataRow secondRow = rows.get(1);
        assertThat(secondRow.getValues(), hasSize(3));
        assertThat(secondRow.get(FIRST_ALIAS), equalTo("two"));
        assertThat(secondRow.get(SECOND_ALIAS), equalTo(222d));
        assertThat(secondRow.get(THIRD_ALIAS), equalTo(42));
    }

    @Test
    public void testSelectAll() throws Exception {
        Map<String, Object> selectionMap = new HashMap<String, Object>() {
            {
                put("*", new ColumnExpression());
            }
        };
        SelectTask selectTask = applicationContext.getBean(
                SelectTask.class,
                selectionMap
        );
        ExecutionResult inputData = getInput();
        ExecutionResult executionResult = selectTask.execute(inputData);
        assertThat(executionResult, equalTo(inputData));
    }

    @Test
    public void testSelectAllFromOneTable() throws Exception {
        Map<String, Object> selectionMap = new HashMap<String, Object>() {
            {
                put("*", new ColumnExpression(Column.ANY, TABLE_NAME));
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
        DataRow firstRow = rows.get(0);
        assertThat(firstRow.getValues(), hasSize(4));
        assertThat(firstRow.get(FIRST_COLUMN), equalTo("one"));
        assertThat(firstRow.get(SECOND_COLUMN), equalTo(444f));
        assertThat(firstRow.get(THIRD_COLUMN), equalTo(555));
        assertThat(firstRow.get(FOURTH_COLUMN), equalTo(-333L));

        DataRow secondRow = rows.get(1);
        assertThat(secondRow.getValues(), hasSize(4));
        assertThat(secondRow.get(FIRST_COLUMN), equalTo("two"));
        assertThat(secondRow.get(SECOND_COLUMN), equalTo(666f));
        assertThat(secondRow.get(THIRD_COLUMN), equalTo(777));
        assertThat(secondRow.get(FOURTH_COLUMN), equalTo(222L));
    }

    private static ExecutionResult getInput() {
        ExecutionResult input = new ExecutionResult();
        input.setCount(2);
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>() {
            {
                put(TABLE_NAME, Arrays.asList(FIRST_COLUMN, SECOND_COLUMN, THIRD_COLUMN, FOURTH_COLUMN));
            }
        };
        DataContainer dataContainer = new DataContainer(columnsMap);
        dataContainer.addRow(createRow("one", 444f, 555, -333L));
        dataContainer.addRow(createRow("two", 666f, 777, 222L));
        input.setData(dataContainer);
        return input;
    }

    private static List<Object> createRow(String first, Float second, Integer third, Long fourth) {
        return new ArrayList<Object>(){
            {
                add(first);
                add(second);
                add(third);
                add(fourth);
            }
        };
    }

}