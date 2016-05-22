package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.NETWORKS;

@Component
@ForeignKey(columns = "project_id", table = "projects", tableColumns = "id")
@Index(columnNames = "name")
@Index(columnNames = "state")
public class NetworksTable implements Table {
    
    public String id;
    public String project_id;
    public String name;
    public String state;
    public Boolean is_shared;
    
    @Override
    public String getName() {
        return NETWORKS.getTableName();
    }
    
}
