package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.ExecutionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LimitTaskTest {
    
    private static final int INITIAL_SIZE = 10;
    private static final String KEY = "key";
    private static final int COUNT = 2;
    private static final int OFFSET = 5;

    private ExecutionResult getInput() {
        ExecutionResult input = new ExecutionResult();
        input.setCount(INITIAL_SIZE);
        DataContainer dataContainer = new DataContainer(Collections.singletonList(KEY));
        for (int n = 1; n <= INITIAL_SIZE; n++) {
            final int value = n;
            dataContainer.addRow(new ArrayList<Object>(){
                {
                    add(value);
                }
            });
        }
        input.setData(dataContainer);
        return input;
    }
    
    @Test
    public void testOffsetCount() throws Exception {
        ExecutionResult input = getInput();
        LimitTask limitTask = new LimitTask(OFFSET, COUNT);
        ExecutionResult output = limitTask.execute(input);
        doChecks(output, 6, 7);
    }
    
    @Test
    public void testCountOnly() throws Exception {
        ExecutionResult input = getInput();
        LimitTask limitTask = new LimitTask(COUNT);
        ExecutionResult output = limitTask.execute(input);
        doChecks(output, 1, 2);
    }
    
    private void doChecks(ExecutionResult output, int firstValue, int secondValue) {
        assertThat(output.getCount(), equalTo(COUNT));
        List<DataRow> data = output.getData().getRows();
        assertThat(data.size(), equalTo(COUNT));
        assertThat(data.get(0).get(KEY), equalTo(firstValue));
        assertThat(data.get(1).get(KEY), equalTo(secondValue));
    }
}