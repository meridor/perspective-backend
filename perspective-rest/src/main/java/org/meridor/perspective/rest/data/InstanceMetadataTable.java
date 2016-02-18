package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.INSTANCE_METADATA;
import static org.meridor.perspective.sql.impl.table.TableName.INSTANCE_NETWORKS;

@Component
public class InstanceMetadataTable implements Table {
    
    public String instance_id;
    public String key;
    public String value;
    
    @Override
    public TableName getName() {
        return INSTANCE_METADATA;
    }
    
}
