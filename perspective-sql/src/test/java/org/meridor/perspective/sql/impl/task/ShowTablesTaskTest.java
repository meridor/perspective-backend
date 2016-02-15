package org.meridor.perspective.sql.impl.task;

import org.junit.Test;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.table.TableName;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ShowTablesTaskTest {

    @Test
    public void testExecute() throws Exception {
        ShowTablesTask showTablesTask = new ShowTablesTask();
        final int TABLES_COUNT = TableName.values().length;
        ExecutionResult executionResult = showTablesTask.execute(new ExecutionResult());
        assertThat(executionResult.getCount(), equalTo(TABLES_COUNT));
        assertThat(executionResult.getData().getRows(), hasSize(TABLES_COUNT));
        assertThat(executionResult.getData().getColumnNames(), contains("table_name"));
    }
}