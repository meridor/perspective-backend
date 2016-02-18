package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.CLOUDS;

@Component
public class CloudsTable implements Table {
    
    public String id;
    public String type;
    
    @Override
    public TableName getName() {
        return CLOUDS;
    }
    
}
