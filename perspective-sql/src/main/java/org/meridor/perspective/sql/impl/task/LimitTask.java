package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.ExecutionResult;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.stream.Collectors;

public class LimitTask implements Task {
    
    private final int offset;
    
    private final int count;

    public LimitTask(int offset, int count) {
        this.offset = offset;
        this.count = count;
    }
    
    public LimitTask(int count) {
        this(0, count);
    }

    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        DataContainer newData = new DataContainer(
                previousTaskResult.getData(),
                rows -> rows.stream()
                        .skip(offset)
                        .limit(count)
                        .collect(Collectors.toList()) 
        );
        return new ExecutionResult(){
            {
                setData(newData);
                setCount(newData.getRows().size());
            }
        };
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "LimitTask{" +
                "offset=" + offset +
                ", count=" + count +
                '}';
    }
}
