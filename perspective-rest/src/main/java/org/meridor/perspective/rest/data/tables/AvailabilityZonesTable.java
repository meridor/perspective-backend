package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.AVAILABILITY_ZONES;

@Component
public class AvailabilityZonesTable implements Table {
    
    public String name;
    public String project_id;
    
    @Override
    public String getName() {
        return AVAILABILITY_ZONES.getTableName();
    }
    
}
