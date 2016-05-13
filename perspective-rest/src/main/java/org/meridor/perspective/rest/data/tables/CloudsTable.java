package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.CLOUDS;

@Component
public class CloudsTable implements Table {
    
    public String id;
    public String type;
    
    @Override
    public String getName() {
        return CLOUDS.getTableName();
    }
    
}
