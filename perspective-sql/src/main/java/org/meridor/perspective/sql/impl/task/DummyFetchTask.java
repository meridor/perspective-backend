package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.ExecutionResult;

import java.sql.SQLException;
import java.util.Collections;

public class DummyFetchTask implements Task {
    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        return new ExecutionResult() {
            {
                setCount(1);
                setData(new DataContainer(Collections.singletonMap("", Collections.singletonList(""))) {
                    {
                        addRow(Collections.singletonList(""));
                    }
                });
            }
        };
        
    }

    @Override
    public String toString() {
        return "DummyFetchTask{}";
    }
}
