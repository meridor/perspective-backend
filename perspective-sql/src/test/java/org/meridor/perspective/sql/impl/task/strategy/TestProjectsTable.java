package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.impl.table.Table;

public class TestProjectsTable implements Table {
    
    public String id;
    public String project_name;
    
    @Override
    public String getName() {
        return "projects";
    }
    
}
