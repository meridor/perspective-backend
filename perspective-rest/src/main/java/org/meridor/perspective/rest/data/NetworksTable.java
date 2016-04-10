package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.NETWORKS;

@Component
public class NetworksTable implements Table {
    
    public String id;
    public String project_id;
    public String name;
    public String state;
    public Boolean is_shared;
    
    @Override
    public TableName getName() {
        return NETWORKS;
    }
    
}
