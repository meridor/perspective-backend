package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.INSTANCE_NETWORKS;

@Component
public class InstanceNetworksTable implements Table {
    
    public String instance_id;
    public String network_id;
    
    @Override
    public TableName getName() {
        return INSTANCE_NETWORKS;
    }
    
}
