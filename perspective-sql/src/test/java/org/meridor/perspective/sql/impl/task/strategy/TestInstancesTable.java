package org.meridor.perspective.sql.impl.task.strategy;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.Index;

@Index(columnNames = "id")
@Index(columnNames = "name")
@Index(columnNames = "project_id")
public class TestInstancesTable implements Table {
    
    public String id;
    public String name;
    public String project_id;
    
    
    @Override
    public String getName() {
        return "instances";
    }
    
}
