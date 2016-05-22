package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.INSTANCE_METADATA;

@Component
@ForeignKey(columns = "instance_id", table = "instances", tableColumns = "id")
@Index(columnNames = {"instance_id", "key"})
public class InstanceMetadataTable implements Table {
    
    public String instance_id;
    public String key;
    public String value;
    
    @Override
    public String getName() {
        return INSTANCE_METADATA.getTableName();
    }
    
}
