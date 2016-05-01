package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.INSTANCE_METADATA;

@Component
public class InstanceMetadataTable implements Table {
    
    public String instance_id;
    public String key;
    public String value;
    
    @Override
    public String getName() {
        return INSTANCE_METADATA.getTableName();
    }
    
}
