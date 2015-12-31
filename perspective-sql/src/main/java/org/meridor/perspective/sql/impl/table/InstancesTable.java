package org.meridor.perspective.sql.impl.table;

import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.INSTANCES;

@Component
public class InstancesTable implements Table {
    
    public String id;
    
    @Override
    public TableName getName() {
        return INSTANCES;
    }
    
}
