package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.impl.task.Task;

import java.sql.SQLSyntaxErrorException;
import java.util.Queue;

public interface QueryScheduler {
    
    Queue<Task> schedule() throws SQLSyntaxErrorException;
    
}
