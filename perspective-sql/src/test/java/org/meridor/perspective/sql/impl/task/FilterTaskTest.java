package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class FilterTaskTest {

    private static final String FIRST_COLUMN = "id";
    private static final String SECOND_COLUMN = "name";

    @Autowired
    private ApplicationContext applicationContext;

    private ExecutionResult getInput() {
        return new ExecutionResult() {
            {
                setCount(3);
                setData(new DataContainer(Arrays.asList(FIRST_COLUMN, SECOND_COLUMN)) {
                    {
                        addRow(createRow("one", 1));
                        addRow(createRow("two", 2));
                        addRow(createRow("three", 3));
                    }
                });
            }
        };
    }
    
    private List<Object> createRow(String first, Integer second) {
        return Arrays.asList(first, second);
    }

    @Test
    public void testExecute() throws Exception {
        FilterTask filterTask = applicationContext.getBean(FilterTask.class);
        filterTask.setCondition(dr -> 
                String.valueOf(dr.get(FIRST_COLUMN)).contains("t") &&
                Integer.valueOf(dr.get(SECOND_COLUMN).toString()) <= 2
        );
        ExecutionResult output = filterTask.execute(getInput());
        assertThat(output.getCount(), equalTo(1));
        assertThat(output.getData().getRows().size(), equalTo(1));
        DataRow data = output.getData().getRows().get(0);
        assertThat(data.get(FIRST_COLUMN), equalTo("two"));
        assertThat(data.get(SECOND_COLUMN), equalTo(2));
    }
}