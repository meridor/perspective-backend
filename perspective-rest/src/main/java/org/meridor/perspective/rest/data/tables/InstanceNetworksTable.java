package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.INSTANCE_NETWORKS;

@Component
public class InstanceNetworksTable implements Table {
    
    public String instance_id;
    public String network_id;
    
    @Override
    public String getName() {
        return INSTANCE_NETWORKS.getTableName();
    }
    
}
